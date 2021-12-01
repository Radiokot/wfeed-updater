package ua.com.radiokot.feed.updater.vk.walls.service

import ua.com.radiokot.feed.updater.vk.walls.model.VkNewsfeedPage

interface VkNewsfeedService {
    /**
     * @return newsfeed with posts in descending order, not older than [startTimeUnix].
     * Pagination is cursor-based, the cursor is [startFrom] ([VkNewsfeedPage.nextFrom])
     */
    fun getNewsfeed(
        startTimeUnix: Long? = null,
        count: Int? = null,
        startFrom: String? = null
    ): VkNewsfeedPage
}