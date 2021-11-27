package ua.com.radiokot.feed.updater.posts.model

import ua.com.radiokot.feed.updater.vk.walls.model.VkPost
import java.util.*

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
            val height: Int,
            val width: Int,
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
                        height = maxSize.height,
                        width = maxSize.width,
                        url130 = proportionalSizesMap['m']?.url,
                        url604 = proportionalSizesMap['x']?.url,
                        url807 = proportionalSizesMap['y']?.url,
                        url1280 = proportionalSizesMap['z']?.url,
                        url2560 = proportionalSizesMap['w']?.url,
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
}