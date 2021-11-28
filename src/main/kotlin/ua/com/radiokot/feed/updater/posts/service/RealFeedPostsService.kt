package ua.com.radiokot.feed.updater.posts.service

import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import java.sql.Connection
import javax.sql.DataSource

class RealFeedPostsService(
    private val dataSource: DataSource
) : FeedPostsService {
    override fun savePosts(posts: List<FeedPostToSave>) {
        TODO()
    }
}