package ua.com.radiokot.feed.updater.vk

import ua.com.radiokot.feed.updater.authors.model.FeedAuthor
import ua.com.radiokot.feed.updater.authors.model.FeedSite
import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import ua.com.radiokot.feed.updater.posts.service.FeedPostsService
import ua.com.radiokot.feed.updater.util.ShittyPostChecker
import ua.com.radiokot.feed.updater.vk.walls.model.VkAuthor
import ua.com.radiokot.feed.updater.vk.walls.model.VkPost
import ua.com.radiokot.feed.updater.vk.walls.service.VkNewsfeedService
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Updates feed from VK newsfeed posts.
 */
class VkUpdater(
    private val vkNewsfeedService: VkNewsfeedService,
    private val feedPostsService: FeedPostsService,
) {
    private data class VkNewsfeed(
        val posts: List<VkPost>,
        val authorsById: Map<String, VkAuthor>,
    )

    fun update(
        feedAuthors: List<FeedAuthor>,
        startTimeUnix: Long
    ) {
        Logger.getGlobal()
            .log(
                Level.INFO, "VkUpdater.update(): " +
                        "start, " +
                        "startTimeUnix=$startTimeUnix, " +
                        "feedAuthors=${feedAuthors.size}"
            )

        val newsfeed = getNewsfeed(startTimeUnix)

        Logger.getGlobal()
            .log(
                Level.INFO, "VkUpdater.update(): " +
                        "got_newsfeed, " +
                        "posts=${newsfeed.posts.size}"
            )

        val postsByVkAuthor = newsfeed.posts
            .groupBy { post ->
                newsfeed.authorsById.getValue(post.ownerId.trimStart('-'))
            }

        val feedAuthorsByApiId = feedAuthors
            .filter { it.site == FeedSite.VK }
            .associateBy(FeedAuthor::apiId)

        val filteredPostsByFeedAuthor = postsByVkAuthor
            .filterKeys { vkAuthor ->
                feedAuthorsByApiId.containsKey(vkAuthor.id)
            }
            .mapKeys { (vkAuthor, _) ->
                feedAuthorsByApiId.getValue(vkAuthor.id)
            }
            .mapValues { (_, posts) ->
                posts
                    .filter { !it.markedAsAds && !ShittyPostChecker.isTextShitty(it.text) }
            }
            .filterValues { posts ->
                posts.isNotEmpty()
            }

        Logger.getGlobal()
            .log(
                Level.INFO, "VkUpdater.update(): " +
                        "filtered_posts, " +
                        "foundFeedAuthors=${filteredPostsByFeedAuthor.size}"
            )

        filteredPostsByFeedAuthor
            .forEach { (feedAuthor, posts) ->

                Logger.getGlobal()
                    .log(
                        Level.INFO, "VkUpdater.update(): " +
                                "call_save_posts, " +
                                "feedAuthor=$feedAuthor, " +
                                "posts=${posts.size}"
                    )

                feedPostsService.savePosts(
                    posts.map { post ->
                        FeedPostToSave(post, feedAuthor)
                    }
                )
            }
    }

    private fun getNewsfeed(startTimeUnix: Long): VkNewsfeed {
        var nextFrom: String? = null

        val posts = mutableListOf<VkPost>()
        val authorsById = mutableMapOf<String, VkAuthor>()

        while (true) {
            val page = vkNewsfeedService
                .getNewsfeed(
                    startTimeUnix = startTimeUnix,
                    count = 50,
                    startFrom = nextFrom
                )

            posts.addAll(page.posts)
            page.authors.associateByTo(authorsById, VkAuthor::id)

            nextFrom = page.nextFrom

            if (nextFrom == null) {
                return VkNewsfeed(posts, authorsById)
            }
        }
    }
}