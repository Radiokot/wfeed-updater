package ua.com.radiokot.feed.updater.tumblr

import mu.KotlinLogging
import ua.com.radiokot.feed.updater.authors.model.FeedAuthor
import ua.com.radiokot.feed.updater.authors.model.FeedSite
import ua.com.radiokot.feed.updater.authors.service.FeedAuthorsService
import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import ua.com.radiokot.feed.updater.posts.service.FeedPostsService
import ua.com.radiokot.feed.updater.tumblr.dashboard.model.TumblrPost
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.TumblrDashboardService

/**
 * Updates feed from Tumblr dashboard posts.
 */
class TumblrUpdater(
    private val tumblrDashboardService: TumblrDashboardService,
    private val feedPostsService: FeedPostsService,
    private val feedAuthorsService: FeedAuthorsService,
) {
    private val logger = KotlinLogging.logger("TumblrUpdater")

    /**
     * @param startPostId post ID to load the dashboard from,
     * if null then will load no more than [MAX_DASHBOARD_POSTS_WITHOUT_START_ID] posts.
     */
    fun update(
        startPostId: String?
    ) {
        logger.debug {
            "start: " +
                    "startPostId=$startPostId"
        }

        val feedAuthors = feedAuthorsService.getAuthors(FeedSite.TUMBLR)

        logger.debug {
            "got_tumblr_feed_authors: " +
                    "feedAuthors=${feedAuthors.size}"
        }

        if (feedAuthors.isEmpty()) {
            return
        }

        val dashboardPosts = getDashboardPosts(startPostId)

        logger.debug {
            "got_dashboard: " +
                    "posts=${dashboardPosts.size}"
        }

        if (dashboardPosts.isEmpty()) {
            return
        }

        val postsByBlogName = dashboardPosts
            .groupBy(TumblrPost::blogName)

        val feedAuthorsByApiId = feedAuthors
            .associateBy(FeedAuthor::apiId)

        val filteredPostsByFeedAuthor = postsByBlogName
            .filterKeys { blogName ->
                feedAuthorsByApiId.containsKey(blogName)
            }
            .mapKeys { (blogName, _) ->
                feedAuthorsByApiId.getValue(blogName)
            }
            .mapValues { (_, posts) ->
                posts
                    .filterNot { post ->
                        val isFilteredOut = post.photos.isEmpty()

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
                        tumblrPost = post,
                        author = feedAuthor,
                    )
                }
            }
            .flatten()

        logger.info {
            "collected_posts_to_save: " +
                    "posts=${postsToSave.size}"
        }

        feedPostsService.savePosts(postsToSave)
    }

    private fun getDashboardPosts(
        startPostId: String?
    ): List<TumblrPost> {
        // When loading paged resource with descending offset-based pagination,
        // we must ensure that we eliminate duplicates.
        val postsSet = mutableSetOf<TumblrPost>()

        var offset = 0
        while (true) {
            val postsPage = tumblrDashboardService.getDashboardPosts(
                limit = DASHBOARD_PAGE_LIMIT,
                offset = offset,
                sinceId = startPostId,
                type = "photo"
            )

            offset += DASHBOARD_PAGE_LIMIT

            // At some point Tumblr begins to return the same page.
            val somethingWasAdded = postsSet.addAll(postsPage)

            if (!somethingWasAdded
                || startPostId == null && postsSet.size >= MAX_DASHBOARD_POSTS_WITHOUT_START_ID
            ) {
                return postsSet.sortedBy(TumblrPost::date)
            }
        }
    }

    private companion object {
        private const val DASHBOARD_PAGE_LIMIT = 20
        private const val MAX_DASHBOARD_POSTS_WITHOUT_START_ID = 250
    }
}