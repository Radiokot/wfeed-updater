package ua.com.radiokot.feed.updater.tumblr.util.oauth1
/*
 * Copyright (C) 2015 Jake Wharton
 * Modified work Copyright 2019 Phil Olson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import okio.ByteString
import java.io.IOException
import java.net.URLEncoder
import java.security.GeneralSecurityException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


class OAuth1SigningInterceptor(
    private val getOAuthKeys: () -> OAuth1Keys,
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(signRequest(chain.request()))
    }

    @Throws(IOException::class)
    fun signRequest(request: Request): Request {
        val keys = getOAuthKeys()

        //Setup default parameters that will be sent with authorization header
        val parameters: HashMap<String, String> = hashMapOf(
            OAUTH_CONSUMER_KEY to keys.consumerKey,
            OAUTH_NONCE to Random.nextLong().toString(),
            OAUTH_SIGNATURE_METHOD to OAUTH_SIGNATURE_METHOD_VALUE,
            OAUTH_TIMESTAMP to (System.currentTimeMillis() / 1000L).toString(),
            OAUTH_VERSION to OAUTH_VERSION_VALUE
        )
        keys.accessToken?.let { parameters[OAUTH_TOKEN] = it }

        //Copy query parameters into param map
        val url = request.url
        for (i in 0 until url.querySize) {
            parameters[url.queryParameterName(i)] = url.queryParameterValue(i)!!
        }

        //Copy form body into param map
        request.body
            ?.asString()
            ?.split('&')
            ?.takeIf { it.isNotEmpty() }
            ?.map { it.split('=', limit = 2) }
            ?.filter {
                (it.size == 2).also { hasTwoParts ->
                    if (!hasTwoParts) throw IllegalStateException(
                        "Key with no value: ${
                            it.getOrNull(
                                0
                            )
                        }"
                    )
                }
            }
            ?.associate {
                val (key, value) = it
                key to value
            }
            ?.also { parameters.putAll(it) }

        //Create signature
        val method = request.method.encodeUtf8()
        val baseUrl = request.url.newBuilder().query(null).build().toString().encodeUtf8()
        val signingKey = "${keys.consumerSecret.encodeUtf8()}&${
            keys.accessSecret?.encodeUtf8()
                ?: ""
        }"
        val params = parameters.encodeForSignature()
        val dataToSign = "$method&$baseUrl&$params"
        parameters[OAUTH_SIGNATURE] = sign(signingKey, dataToSign).encodeUtf8()

        //Create auth header
        val authHeader = "OAuth ${parameters.toHeaderFormat()}"
        return request.newBuilder().addHeader("Authorization", authHeader).build()
    }

    private fun RequestBody.asString() = Buffer().run {
        writeTo(this)
        readUtf8().replace("+", "%2B")
    }

    @Throws(GeneralSecurityException::class)
    private fun sign(key: String, data: String): String {
        val secretKey = SecretKeySpec(key.toBytesUtf8(), "HmacSHA1")
        val macResult = Mac.getInstance("HmacSHA1").run {
            init(secretKey)
            doFinal(data.toBytesUtf8())
        }
        return ByteString.of(*macResult).base64()
    }

    private fun String.toBytesUtf8() = this.toByteArray()

    private fun HashMap<String, String>.toHeaderFormat() =
        filter { it.key in baseKeys }
            .toList()
            .sortedBy { (key, _) -> key }
            .toMap()
            .map { "${it.key}=\"${it.value}\"" }
            .joinToString(", ")


    private fun HashMap<String, String>.encodeForSignature() =
        toList()
            .sortedBy { (key, _) -> key }
            .toMap()
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .encodeUtf8()

    private fun String.encodeUtf8() = URLEncoder.encode(this, "UTF-8").replace("+", "%2B")

    companion object {
        private const val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
        private const val OAUTH_NONCE = "oauth_nonce"
        private const val OAUTH_SIGNATURE = "oauth_signature"
        private const val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
        private const val OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1"
        private const val OAUTH_TIMESTAMP = "oauth_timestamp"
        private const val OAUTH_TOKEN = "oauth_token"
        private const val OAUTH_VERSION = "oauth_version"
        private const val OAUTH_VERSION_VALUE = "1.0"

        private val baseKeys = arrayListOf(
            OAUTH_CONSUMER_KEY,
            OAUTH_NONCE,
            OAUTH_SIGNATURE,
            OAUTH_SIGNATURE_METHOD,
            OAUTH_TIMESTAMP,
            OAUTH_TOKEN,
            OAUTH_VERSION
        )
    }
}
