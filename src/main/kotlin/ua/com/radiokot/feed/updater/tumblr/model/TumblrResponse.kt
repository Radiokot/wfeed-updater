package ua.com.radiokot.feed.updater.tumblr.model

import com.fasterxml.jackson.annotation.JsonProperty

class TumblrResponse<T>(
    @JsonProperty("response")
    val response: T
)