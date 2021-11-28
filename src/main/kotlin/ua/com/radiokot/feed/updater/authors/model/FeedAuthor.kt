package ua.com.radiokot.feed.updater.authors.model

import java.sql.ResultSet
import java.util.*

data class FeedAuthor(
    val id: String,
    val apiId: String,
    val site: FeedSite,
    val name: String,
    val photoUrl: String,
    val lastPostDate: Date,
) {
    constructor(authorsResultSet: ResultSet) : this(
        id = authorsResultSet.getString("id"),
        apiId = authorsResultSet.getString("apiId"),
        site = authorsResultSet.getInt("siteId").let { FeedSite.valueOf(it) },
        name = authorsResultSet.getString("authorName"),
        photoUrl = authorsResultSet.getString("authorPhoto"),
        lastPostDate = Date(authorsResultSet.getLong("authorLastPostDate") * 1000)
    )

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

    override fun toString(): String {
        return "FeedAuthor(id='$id', name='$name')"
    }
}