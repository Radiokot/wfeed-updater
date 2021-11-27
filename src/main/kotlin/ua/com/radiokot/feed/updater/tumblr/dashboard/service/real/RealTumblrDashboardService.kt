package ua.com.radiokot.feed.updater.tumblr.dashboard.service.real

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ua.com.radiokot.feed.updater.extensions.addNotNullQueryParameter
import ua.com.radiokot.feed.updater.tumblr.dashboard.model.TumblrPost
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.TumblrDashboardService
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.real.model.TumblrDashboardResponseData
import ua.com.radiokot.feed.updater.tumblr.model.TumblrResponse

class RealTumblrDashboardService(
    private val tumblrHttpClient: OkHttpClient,
    private val mapper: ObjectMapper,
) : TumblrDashboardService {
    override fun getDashboardPosts(
        limit: Int?,
        sinceId: String?,
        type: String?
    ): List<TumblrPost> {
        require(limit == null || limit in (1..20)) {
            "The limit must be in range 1–20, inclusive"
        }

        val request = Request.Builder()
            .get()
            .url(
                "https://api.tumblr.com/v2/user/dashboard".toHttpUrl().newBuilder()
                    .addNotNullQueryParameter("since_id", sinceId)
                    .addNotNullQueryParameter("limit", limit)
                    .addNotNullQueryParameter("type", type)
                    .build()
            )
            .build()

        return tumblrHttpClient
            .newCall(request)
            .execute()
            .body
            .let { checkNotNull(it) { "Tumblr dashboard response must have a body" } }
            .byteStream()
            .let { mapper.readValue<TumblrResponse<TumblrDashboardResponseData>>(it) }
            .response
            .posts
    }
}