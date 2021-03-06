package ua.com.radiokot.feed.updater.authors.model

import ua.com.radiokot.feed.updater.vk.walls.model.VkAuthor

/**
 * @see [FeedAuthor.isUpdateRequired]
 */
data class FeedAuthorDataToUpdate(
    val name: String,
    val photoUrl: String
) {
    constructor(
        vkAuthor: VkAuthor,
        vkPhotoProxyUrl: String?,
    ) : this(
        name = vkAuthor.name,
        photoUrl =
        if (vkPhotoProxyUrl != null)
            vkPhotoProxyUrl + vkAuthor.photoUrl
        else
            vkAuthor.photoUrl
    )
}