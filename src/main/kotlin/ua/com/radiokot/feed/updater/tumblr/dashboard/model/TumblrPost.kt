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
    @JsonProperty("photos")
    val photos: List<Photo>?,
    @JsonProperty("post_url")
    val url: String,
) {
    data class Photo(
        @JsonProperty("original_size")
        val originalSize: SizeLink,
        @JsonProperty("alt_sizes")
        val altSizes: List<SizeLink>
    ) {
        data class SizeLink(
            @JsonProperty("height")
            val height: Int,
            @JsonProperty("width")
            val width: Int,
            @JsonProperty("url")
            val url: String
        )
    }
}