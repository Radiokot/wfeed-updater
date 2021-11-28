package ua.com.radiokot.feed.updater.vk.walls.service

import ua.com.radiokot.feed.updater.vk.walls.model.VkNewsfeedPage

interface VkNewsfeedService {
    fun getNewsfeed(
        startTime: Long? = null,
        count: Int? = null,
        startFrom: String? = null
    ): VkNewsfeedPage
}