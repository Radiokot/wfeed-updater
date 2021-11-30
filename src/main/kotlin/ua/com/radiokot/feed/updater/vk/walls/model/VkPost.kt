package ua.com.radiokot.feed.updater.vk.walls.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ua.com.radiokot.feed.updater.util.json.UnixTimestampDateDeserializer
import ua.com.radiokot.feed.updater.vk.walls.util.VkPostAttachmentsDeserializer
import java.util.*

data class VkPost(
    @JsonProperty("id")
    @JsonAlias("post_id")
    val id: String,
    @JsonProperty("owner_id")
    @JsonAlias("source_id")
    val ownerId: String,
    @JsonProperty("date")
    @JsonDeserialize(using = UnixTimestampDateDeserializer::class)
    val date: Date,
    @JsonProperty("marked_as_ads")
    val markedAsAds: Boolean,
    @JsonProperty("text")
    val text: String,
    @JsonProperty("attachments")
    @JsonDeserialize(using = VkPostAttachmentsDeserializer::class)
    val attachments: List<Attachment> = emptyList()
) {
    sealed class Attachment {
        data class Photo(
            @JsonProperty("id")
            val id: String,
            @JsonProperty("owner_id")
            val ownerId: String,
            @JsonProperty("sizes")
            val sizes: List<SizeLink>
        ) : Attachment() {
            data class SizeLink(
                @JsonProperty("width")
                val width: Int,
                @JsonProperty("height")
                val height: Int,
                @JsonProperty("url")
                val url: String,
                @JsonProperty("type")
                val type: Char
            )

            companion object {
                const val TYPE = "photo"
            }
        }
    }

    val url: String
        get() = "https://vk.com/wall${ownerId}_${id}"
}