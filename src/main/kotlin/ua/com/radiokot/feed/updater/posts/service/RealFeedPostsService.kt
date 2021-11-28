package ua.com.radiokot.feed.updater.posts.service

import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import java.sql.Connection

class RealFeedPostsService(
    private val databaseConnection: Connection
) : FeedPostsService {
    override fun savePosts(posts: List<FeedPostToSave>) {
        TODO()
    }
}