package ua.com.radiokot.feed.updater.vk.walls.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ua.com.radiokot.feed.updater.extensions.addNotNullQueryParameter
import ua.com.radiokot.feed.updater.vk.model.VkResponse
import ua.com.radiokot.feed.updater.vk.util.VkRequestRateLimiter
import ua.com.radiokot.feed.updater.vk.walls.model.VkWall

class RealVkWallsService(
    private val vkHttpClient: OkHttpClient,
    private val mapper: ObjectMapper
) : VkWallsService {
    override fun getGroupWalls(
        groupIds: Collection<String>,
        wallPostsLimit: Int
    ): List<VkWall> {
        require(groupIds.size <= MAX_GROUPS) {
            "The request can't load more than $MAX_GROUPS groups"
        }

        require(wallPostsLimit <= MAX_POSTS) {
            "The request can't load more than $MAX_POSTS posts for each wall"
        }

        val request = Request.Builder()
            .get()
            .url(
                "https://api.vk.com/method/execute.getGroupsWalls".toHttpUrl().newBuilder()
                    .addNotNullQueryParameter("v", VK_API_VERSION)
                    .addNotNullQueryParameter("pcount", wallPostsLimit)
                    .addNotNullQueryParameter("scount", groupIds.size)
                    .apply {
                        groupIds.forEachIndexed { i, groupId ->
                            addNotNullQueryParameter("s${i + 1}", groupId)
                        }
                    }
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
            .let { mapper.readValue<VkResponse<List<VkWall>>>(it) }
            .response
    }

    private companion object {
        private const val MAX_GROUPS = 24
        private const val MAX_POSTS = 10
        private const val VK_API_VERSION = "5.131"
    }
}