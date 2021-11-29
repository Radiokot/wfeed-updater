package ua.com.radiokot.feed.updater.vk.walls.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ua.com.radiokot.feed.updater.extensions.addNotNullQueryParameter
import ua.com.radiokot.feed.updater.vk.model.VkResponse
import ua.com.radiokot.feed.updater.vk.util.VkRequestRateLimiter
import ua.com.radiokot.feed.updater.vk.walls.model.VkNewsfeedPage

class RealVkNewsfeedService(
    private val vkHttpClient: OkHttpClient,
    private val mapper: ObjectMapper
) : VkNewsfeedService {
    override fun getNewsfeed(
        startTimeUnix: Long?,
        count: Int?,
        startFrom: String?
    ): VkNewsfeedPage {
        require(count == null || count <= MAX_COUNT) {
            "The request can't load more than $MAX_COUNT posts for each wall"
        }

        val request = Request.Builder()
            .get()
            .url(
                "https://api.vk.com/method/newsfeed.get".toHttpUrl().newBuilder()
                    .addQueryParameter("v", VK_API_VERSION)
                    .addQueryParameter("filters", "post")
                    .addQueryParameter("fields", "photo_100")
                    .addNotNullQueryParameter("start_time", startTimeUnix)
                    .addNotNullQueryParameter("start_from", startFrom)
                    .addNotNullQueryParameter("count", count)
                    .build()
            )
            .build()

        VkRequestRateLimiter.waitBeforeRequest()

        return vkHttpClient
            .newCall(request)
            .execute()
            .body
            .let { checkNotNull(it) { "VK walls response must have a body" } }
            .byteStream()
            .let { mapper.readValue<VkResponse<VkNewsfeedPage>>(it) }
            .response
    }

    private companion object {
        private const val VK_API_VERSION = "5.131"
        private const val MAX_COUNT = 100
    }
}