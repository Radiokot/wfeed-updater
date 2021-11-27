package ua.com.radiokot.feed.updater.vk.util

import okhttp3.Interceptor
import okhttp3.Response

/**
 * @param accessTokens access tokens from which
 * the single one is randomly picked for each request
 */
class OAuth2MultipleTokensInterceptor(
    private val accessTokens: List<String>,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        return chain.proceed(
            chain.request().newBuilder()
                .url(
                    url.newBuilder()
                        .addQueryParameter("access_token", accessTokens.random())
                        .build()
                )
                .build()
        )
    }
}