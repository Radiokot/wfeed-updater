package ua.com.radiokot.feed.updater.tumblr.dashboard.service.real.model

import com.fasterxml.jackson.annotation.JsonProperty
import ua.com.radiokot.feed.updater.tumblr.dashboard.model.TumblrPost

class TumblrDashboardResponseData(
    @JsonProperty("posts")
    val posts: List<TumblrPost>
)