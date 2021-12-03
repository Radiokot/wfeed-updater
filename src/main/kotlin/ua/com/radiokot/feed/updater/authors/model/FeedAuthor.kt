package ua.com.radiokot.feed.updater.authors.model

import java.sql.ResultSet

data class FeedAuthor(
    val id: Int,
    val apiId: String,
    val site: FeedSite,
    val name: String,
    val photoUrl: String,
) {
    constructor(authorsResultSet: ResultSet) : this(
        id = authorsResultSet.getInt("id"),
        apiId = authorsResultSet.getString("api_id"),
        site = authorsResultSet.getInt("site_id").let { FeedSite.valueOf(it) },
        name = authorsResultSet.getString("name"),
        photoUrl = authorsResultSet.getString("photo"),
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

    fun isUpdateRequired(dataToUpdate: FeedAuthorDataToUpdate): Boolean =
        name != dataToUpdate.name
                || photoUrl != dataToUpdate.photoUrl
}