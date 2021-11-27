package ua.com.radiokot.feed.updater.vk.walls.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ua.com.radiokot.feed.updater.util.json.UnixTimestampDateDeserializer
import java.util.*

data class VkPost(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("from_id")
    val fromId: String,
    @JsonProperty("owner_id")
    val ownerId: String,
    @JsonProperty("date")
    @JsonDeserialize(using = UnixTimestampDateDeserializer::class)
    val date: Date,
    @JsonProperty("marked_as_ads")
    val markedAsAds: Boolean,
    @JsonProperty("text")
    val text: String,
    @JsonProperty("attachments")
    val attachments: List<Attachment>
) {
    sealed class Attachment(
        @JsonProperty("type")
        val type: String
    ) {
        data class Photo(
            @JsonProperty("id")
            val id: String,
            @JsonProperty("owner_id")
            val ownerId: String,
            @JsonProperty("sizes")
            val sizes: SizeLink
        ) : Attachment("photo") {
            data class SizeLink(
                @JsonProperty("height")
                val height: Int,
                @JsonProperty("width")
                val width: Int,
                @JsonProperty("url")
                val url: String,
                @JsonProperty("type")
                val type: Char
            )
        }
    }
}