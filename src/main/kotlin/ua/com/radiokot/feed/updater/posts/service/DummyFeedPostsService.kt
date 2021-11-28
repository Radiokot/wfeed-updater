package ua.com.radiokot.feed.updater.posts.service

import ua.com.radiokot.feed.updater.authors.model.FeedSite
import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave

class DummyFeedPostsService : FeedPostsService {
    override fun savePosts(posts: List<FeedPostToSave>) {
        val vkSortedPostsByAuthor = posts
            .groupBy(FeedPostToSave::author)
            .filterKeys { author ->
                author.site == FeedSite.VK
            }
            .mapValues { (_, posts) ->
                posts.sortedBy(FeedPostToSave::date)
            }

        val tumblrSortedPosts = posts
            .filter { post ->
                post.author.site == FeedSite.TUMBLR
            }
            .sortedBy(FeedPostToSave::date)

        vkSortedPostsByAuthor
            .forEach { (author, authorsPosts) ->
                val lastPostDate = authorsPosts.last().date
                if (author.lastPostDate < lastPostDate) {
                    println("${author.name}(${author.id} last post date ${author.lastPostDate} => $lastPostDate")
                }
            }

        tumblrSortedPosts
            .lastOrNull()
            ?.date
            ?.also { tumblrLastPostDate ->
                println("Tumblr last post date $tumblrLastPostDate")
            }
    }
}