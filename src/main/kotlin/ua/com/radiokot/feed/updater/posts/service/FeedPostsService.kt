package ua.com.radiokot.feed.updater.posts.service

import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave

interface FeedPostsService {
    fun savePosts(posts: List<FeedPostToSave>)
}