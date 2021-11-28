package ua.com.radiokot.feed.updater.authors.model

import java.util.*

class FeedAuthor(
    val id: String,
    val apiId: String,
    val site: FeedSite,
    val name: String,
    val photoUrl: String,
    val lastPostDate: Date,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeedAuthor

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}