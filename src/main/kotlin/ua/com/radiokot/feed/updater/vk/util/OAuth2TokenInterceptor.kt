package ua.com.radiokot.feed.updater.vk.util

import okhttp3.Interceptor
import okhttp3.Response

class OAuth2TokenInterceptor(
    private val accessToken: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        return chain.proceed(
            chain.request().newBuilder()
                .url(
                    url.newBuilder()
                        .addQueryParameter("access_token", accessToken)
                        .build()
                )
                .build()
        )
    }
}