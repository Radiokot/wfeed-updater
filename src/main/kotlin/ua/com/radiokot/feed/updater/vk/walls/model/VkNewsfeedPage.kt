package ua.com.radiokot.feed.updater.vk.walls.model

import com.fasterxml.jackson.annotation.JsonProperty

class VkNewsfeedPage(
    @JsonProperty("items")
    val posts: List<VkPost>,
    @JsonProperty("groups")
    val authors: List<VkAuthor>,
    @JsonProperty("next_from")
    val nextFrom: String?
)