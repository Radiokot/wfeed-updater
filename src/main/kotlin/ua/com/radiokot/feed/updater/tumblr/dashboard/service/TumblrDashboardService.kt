package ua.com.radiokot.feed.updater.tumblr.dashboard.service

import ua.com.radiokot.feed.updater.tumblr.dashboard.model.TumblrPost

interface TumblrDashboardService {
    fun getDashboardPosts(
        limit: Int? = null,
        sinceId: String? = null,
        type: String? = null
    ): List<TumblrPost>
}