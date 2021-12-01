package ua.com.radiokot.feed.updater.tumblr.dashboard.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ua.com.radiokot.feed.updater.util.json.UnixTimestampDateDeserializer
import java.util.*

data class TumblrPost(
    @JsonProperty("id_string")
    val id: String,
    @JsonProperty("blog_name")
    val blogName: String,
    @JsonProperty("timestamp")
    @JsonDeserialize(using = UnixTimestampDateDeserializer::class)
    val date: Date,
    @JsonProperty("summary")
    val summary: String?,
    @JsonProperty("post_url")
    val url: String,
    @JsonProperty("photos")
    val photos: List<Photo> = emptyList(),
) {
    data class Photo(
        @JsonProperty("alt_sizes")
        val sizes: List<SizeLink>
    ) {
        data class SizeLink(
            @JsonProperty("width")
            val width: Int,
            @JsonProperty("height")
            val height: Int,
            @JsonProperty("url")
            val url: String
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TumblrPost

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}