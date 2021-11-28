package ua.com.radiokot.feed.updater.vk

import ua.com.radiokot.feed.updater.authors.model.FeedAuthor
import ua.com.radiokot.feed.updater.authors.model.FeedAuthorDataToUpdate
import ua.com.radiokot.feed.updater.authors.model.FeedSite
import ua.com.radiokot.feed.updater.authors.service.FeedAuthorsService
import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import ua.com.radiokot.feed.updater.posts.service.FeedPostsService
import ua.com.radiokot.feed.updater.util.ShittyPostChecker
import ua.com.radiokot.feed.updater.vk.walls.model.VkAuthor
import ua.com.radiokot.feed.updater.vk.walls.model.VkPost
import ua.com.radiokot.feed.updater.vk.walls.service.VkNewsfeedService
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Updates feed from VK newsfeed posts,
 * also updates author data if it is updated in VK.
 */
class VkUpdater(
    private val vkNewsfeedService: VkNewsfeedService,
    private val feedPostsService: FeedPostsService,
    private val feedAuthorsService: FeedAuthorsService,
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
                Level.INFO, "start: " +
                        "startTimeUnix=$startTimeUnix, " +
                        "feedAuthors=${feedAuthors.size}"
            )

        val newsfeed = getNewsfeed(startTimeUnix)

        Logger.getGlobal()
            .log(
                Level.INFO, "got_newsfeed: " +
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
                    .filter { post ->
                        !post.markedAsAds
                                && !ShittyPostChecker.isTextShitty(post.text)
                                && post.attachments.isNotEmpty()
                    }
            }
            .filterValues { posts ->
                posts.isNotEmpty()
            }

        Logger.getGlobal()
            .log(
                Level.INFO, "filtered_posts: " +
                        "foundFeedAuthors=${filteredPostsByFeedAuthor.size}"
            )

        val postsToSave = filteredPostsByFeedAuthor
            .map { (feedAuthor, posts) ->
                posts.map { post ->
                    FeedPostToSave(post, feedAuthor)
                }
            }
            .flatten()

        Logger.getGlobal()
            .log(
                Level.INFO, "call_save_posts: " +
                        "posts=${postsToSave.size}"
            )

        feedPostsService.savePosts(postsToSave)

        val authorsToUpdate = postsByVkAuthor
            .keys
            .map { vkAuthor ->
                vkAuthor.id to FeedAuthorDataToUpdate(vkAuthor)
            }
            .mapNotNull { (authorApiId, dataToUpdate) ->
                val feedAuthor = feedAuthorsByApiId[authorApiId]
                if (feedAuthor != null && feedAuthor.isUpdateRequired(dataToUpdate))
                    feedAuthor.id to dataToUpdate
                else
                    null
            }

        if (authorsToUpdate.isNotEmpty()) {
            Logger.getGlobal()
                .log(
                    Level.INFO, "call_update_authors: " +
                            "authorsToUpdate=${authorsToUpdate.size}"
                )

            authorsToUpdate.forEach { (authorId, dataToUpdate) ->
                feedAuthorsService.updateAuthorData(authorId, dataToUpdate)
            }
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