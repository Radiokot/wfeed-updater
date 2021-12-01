package ua.com.radiokot.feed.updater.tumblr.dashboard.service

import ua.com.radiokot.feed.updater.tumblr.dashboard.model.TumblrPost

interface TumblrDashboardService {
    /**
     * @return dashboard posts newer than [sinceId] in descending order.
     * Pagination is offset-based.
     */
    fun getDashboardPosts(
        limit: Int? = null,
        offset: Int? = null,
        sinceId: String? = null,
        type: String? = null
    ): List<TumblrPost>
}