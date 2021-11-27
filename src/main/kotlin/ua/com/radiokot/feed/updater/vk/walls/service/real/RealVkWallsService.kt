package ua.com.radiokot.feed.updater.vk.walls.service.real

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ua.com.radiokot.feed.updater.extensions.addNotNullQueryParameter
import ua.com.radiokot.feed.updater.vk.model.VkResponse
import ua.com.radiokot.feed.updater.vk.walls.model.VkWall
import ua.com.radiokot.feed.updater.vk.walls.service.VkWallsService

class RealVkWallsService(
    private val vkHttpClient: OkHttpClient,
    private val mapper: ObjectMapper
) : VkWallsService {
    override fun getGroupWalls(
        groupIds: Collection<String>,
        wallPostsLimit: Int
    ): List<VkWall> {
        require(groupIds.size <= 24) {
            "The request can't load more than 24 groups"
        }

        require(wallPostsLimit <= 100) {
            "The request can't load more than 100 posts for each wall"
        }

        val request = Request.Builder()
            .get()
            .url(
                "https://api.vk.com/method/execute.getGroupsWalls".toHttpUrl().newBuilder()
                    .addNotNullQueryParameter("pcount", wallPostsLimit)
                    .addNotNullQueryParameter("scount", groupIds.size)
                    .apply {
                        groupIds.forEachIndexed { i, groupId ->
                            addNotNullQueryParameter("s_${i+1}", groupId)
                        }
                    }
                    .build()
            )
            .build()

        return vkHttpClient
            .newCall(request)
            .execute()
            .body
            .let { checkNotNull(it) { "VK walls response must have a body" } }
            .byteStream()
            .let { mapper.readValue<VkResponse<List<VkWall>>>(it) }
            .response
    }
}