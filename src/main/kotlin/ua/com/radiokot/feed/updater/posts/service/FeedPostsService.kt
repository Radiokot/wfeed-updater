package ua.com.radiokot.feed.updater.posts.service

import ua.com.radiokot.feed.updater.authors.model.FeedSite
import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import java.util.*

interface FeedPostsService {
    fun savePosts(posts: List<FeedPostToSave>)

    fun getLastPostApiId(site: FeedSite): String

    fun getLastPostDate(site: FeedSite): Date
}