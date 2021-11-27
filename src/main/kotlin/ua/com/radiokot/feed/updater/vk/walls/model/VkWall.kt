package ua.com.radiokot.feed.updater.vk.walls.model

import com.fasterxml.jackson.annotation.JsonProperty

data class VkWall(
    @JsonProperty("wall")
    val posts: List<VkPost>,
    @JsonProperty("owner")
    val owner: VkAuthor
)