package ua.com.radiokot.feed.updater.vk.model

import com.fasterxml.jackson.annotation.JsonProperty

class VkResponse<T>(
    @JsonProperty("response")
    val response: T
)