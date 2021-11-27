package ua.com.radiokot.feed.updater.vk.walls.model

import com.fasterxml.jackson.annotation.JsonProperty

class VkAuthor(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("screen_name")
    val screenName: String,
    @JsonProperty("photo_50")
    val photo50: String,
    @JsonProperty("photo_100")
    val photo100: String,
    @JsonProperty("photo_200")
    val photo200: String,
)