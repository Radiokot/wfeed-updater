package ua.com.radiokot.feed.updater.vk

import mu.KotlinLogging
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
import java.util.*

/**
 * Updates feed from VK newsfeed posts,
 * also updates author data if it is updated in VK.
 */
class VkUpdater(
    private val vkNewsfeedService: VkNewsfeedService,
    private val feedPostsService: FeedPostsService,
    private val feedAuthorsService: FeedAuthorsService,
    private val vkPhotoProxyUrl: String?,
) {
    private data class VkNewsfeed(
        val posts: List<VkPost>,
        val authorsById: Map<String, VkAuthor>,
    )

    private val logger = KotlinLogging.logger("VkUpdater")

    /**
     * @param startDate time to load the VK feed from
     */
    fun update(
        startDate: Date
    ) {
        logger.debug {
            "start: " +
                    "startTimeUnix=$startDate"
        }

        val feedAuthors = feedAuthorsService.getAuthors(FeedSite.VK)

        logger.debug {
            "got_vk_feed_authors: " +
                    "feedAuthors=${feedAuthors.size}"
        }

        val newsfeed = getNewsfeed(
            startTimeUnix = startDate.time / 1000L
        )

        logger.debug {
            "got_newsfeed: " +
                    "posts=${newsfeed.posts.size}"
        }

        if (newsfeed.posts.isEmpty()) {
            return
        }

        val postsByVkAuthor = newsfeed.posts
            .groupBy { post ->
                newsfeed.authorsById.getValue(post.ownerId.trimStart('-'))
            }

        val feedAuthorsByApiId = feedAuthors
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
                    .filterNot { post ->
                        val isFilteredOut = post.markedAsAds
                                || ShittyPostChecker.isTextShitty(post.text)
                                || post.attachments.isEmpty()

                        if (isFilteredOut) {
                            logger.debug {
                                "post_filtered_out: " +
                                        "url=${post.url}"
                            }
                        }

                        isFilteredOut
                    }
            }
            .filterValues { posts ->
                posts.isNotEmpty()
            }

        val postsToSave = filteredPostsByFeedAuthor
            .map { (feedAuthor, posts) ->
                posts.map { post ->
                    FeedPostToSave(
                        vkPost = post,
                        author = feedAuthor,
                        vkPhotoProxyUrl = vkPhotoProxyUrl
                    )
                }
            }
            .flatten()

        logger.info {
            "collected_posts_to_save: " +
                    "posts=${postsToSave.size}"
        }

        feedPostsService.savePosts(postsToSave)

        val authorsToUpdate = postsByVkAuthor
            .keys
            .map { vkAuthor ->
                vkAuthor.id to FeedAuthorDataToUpdate(
                    vkAuthor = vkAuthor,
                    vkPhotoProxyUrl = vkPhotoProxyUrl
                )
            }
            .mapNotNull { (authorApiId, dataToUpdate) ->
                val feedAuthor = feedAuthorsByApiId[authorApiId]
                if (feedAuthor != null && feedAuthor.isUpdateRequired(dataToUpdate))
                    feedAuthor.id to dataToUpdate
                else
                    null
            }

        if (authorsToUpdate.isNotEmpty()) {
            authorsToUpdate.forEach { (authorId, dataToUpdate) ->
                feedAuthorsService.updateAuthorData(authorId, dataToUpdate)
            }

            logger.info {
                "updated_authors: " +
                        "authorsToUpdate=${authorsToUpdate.size}"
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