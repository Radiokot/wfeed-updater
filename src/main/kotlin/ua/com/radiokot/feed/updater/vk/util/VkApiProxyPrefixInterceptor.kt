package ua.com.radiokot.feed.updater.vk.util

import okhttp3.Interceptor
import okhttp3.Response

class VkApiProxyPrefixInterceptor(
    private val proxyUrl: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.toString()
        return chain.proceed(
            chain.request().newBuilder()
                .url(proxyUrl + url)
                .build()
        )
    }
}