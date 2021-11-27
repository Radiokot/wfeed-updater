package ua.com.radiokot.feed.updater.posts.model

import ua.com.radiokot.feed.updater.tumblr.dashboard.model.TumblrPost
import ua.com.radiokot.feed.updater.vk.walls.model.VkPost
import java.util.*
import kotlin.math.abs

data class FeedPostToSave(
    val apiId: String,
    val text: String?,
    val date: Date,
    val url: String,
    val attachments: List<Attachment>
) {
    sealed class Attachment(
        val type: String
    ) {
        data class Photo(
            val width: Int,
            val height: Int,
            val url130: String?,
            val url604: String?,
            val url807: String?,
            val url1280: String?,
            val url2560: String?,
        ) : Attachment(TYPE) {
            companion object {
                const val TYPE = "photo"

                fun fromVk(vkPhoto: VkPost.Attachment.Photo): Photo {
                    // https://vk.com/dev/photo_sizes
                    val proportionalSizeTypes = linkedSetOf('m', 'x', 'y', 'z', 'w')

                    val sortedProportionalSizes = vkPhoto.sizes
                        .filter { it.type in proportionalSizeTypes }
                        .sortedBy { proportionalSizeTypes.indexOf(it.type) }

                    val proportionalSizesMap = sortedProportionalSizes
                        .associateBy { it.type }

                    val maxSize = sortedProportionalSizes.last()

                    return Photo(
                        width = maxSize.width,
                        height = maxSize.height,
                        url130 = proportionalSizesMap['m']?.url,
                        url604 = proportionalSizesMap['x']?.url,
                        url807 = proportionalSizesMap['y']?.url,
                        url1280 = proportionalSizesMap['z']?.url,
                        url2560 = proportionalSizesMap['w']?.url,
                    )
                }

                fun fromTumblr(tumblrPhoto: TumblrPost.Photo): Photo {
                    val sortedSizes = tumblrPhoto.sizes
                        .sortedBy { it.width }
                        .toMutableList()

                    val maxSize = sortedSizes.last()

                    /**
                     * Find best size for given [width],
                     * remove it and all the smaller from the pool
                     */
                    fun popUrlForSize(width: Int): String? {
                        return sortedSizes
                            .filter { it.width <= width }
                            .also { sortedSizes.removeAll(it) }
                            .minByOrNull { abs(width - it.width) }
                            ?.url
                    }

                    return Photo(
                        width = maxSize.width,
                        height = maxSize.height,
                        url130 = popUrlForSize(130),
                        url604 = popUrlForSize(604),
                        url807 = popUrlForSize(807),
                        url1280 = popUrlForSize(1280),
                        url2560 = popUrlForSize(2560),
                    )
                }
            }
        }

        companion object {
            fun fromVk(vkAttachment: VkPost.Attachment): Attachment {
                return when (vkAttachment) {
                    is VkPost.Attachment.Photo ->
                        Photo.fromVk(vkAttachment)
                }
            }
        }
    }

    constructor(vkPost: VkPost) : this(
        apiId = vkPost.id,
        text = vkPost.text,
        date = vkPost.date,
        url = "https://vk.com/wall${vkPost.ownerId}_${vkPost.id}",
        attachments = vkPost.attachments.map(Attachment.Companion::fromVk)
    )

    constructor(tumblrPost: TumblrPost) : this(
        apiId = tumblrPost.id,
        text = tumblrPost.summary,
        date = tumblrPost.date,
        url = tumblrPost.url,
        attachments = tumblrPost.photos.map { Attachment.Photo.fromTumblr(it) }
    )
}