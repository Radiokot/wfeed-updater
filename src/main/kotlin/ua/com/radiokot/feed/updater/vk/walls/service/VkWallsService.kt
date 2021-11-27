package ua.com.radiokot.feed.updater.vk.walls.service

import ua.com.radiokot.feed.updater.vk.walls.model.VkWall

interface VkWallsService {
    fun getGroupWalls(
        groupIds: Collection<String>,
        wallPostsLimit: Int
    ): List<VkWall>
}