package ua.com.radiokot.feed.updater.vk.walls.model

import com.fasterxml.jackson.annotation.JsonProperty

class VkAuthor(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("screen_name")
    val screenName: String,
    @JsonProperty("photo_100")
    val photoUrl: String,
)