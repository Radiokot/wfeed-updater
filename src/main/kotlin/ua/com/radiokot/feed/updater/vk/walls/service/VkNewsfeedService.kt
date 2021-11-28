package ua.com.radiokot.feed.updater.vk.walls.service

import ua.com.radiokot.feed.updater.vk.walls.model.VkNewsfeedPage

interface VkNewsfeedService {
    fun getNewsfeed(
        startTimeUnix: Long? = null,
        count: Int? = null,
        startFrom: String? = null
    ): VkNewsfeedPage
}