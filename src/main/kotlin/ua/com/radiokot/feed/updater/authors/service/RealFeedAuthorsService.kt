package ua.com.radiokot.feed.updater.authors.service

import ua.com.radiokot.feed.updater.authors.model.FeedAuthor
import ua.com.radiokot.feed.updater.authors.model.FeedAuthorDataToUpdate
import ua.com.radiokot.feed.updater.authors.model.FeedSite
import java.sql.Connection

class RealFeedAuthorsService(
    private val databaseConnection: Connection
) : FeedAuthorsService {
    override fun getAuthors(site: FeedSite?): List<FeedAuthor> {
        var query = "SELECT * FROM author "
        if (site != null) {
            query += "WHERE siteId=? "
        }

        val preparedStatement = databaseConnection
            .prepareStatement(query).apply {
                if (site != null) {
                    setInt(1, site.i)
                }
            }

        return preparedStatement.use { statement ->
            statement.executeQuery().use { resultSet ->
                generateSequence {
                    resultSet
                        .takeIf { it.next() }
                        ?.let(::FeedAuthor)
                }.toList()
            }
        }
    }

    override fun updateAuthorData(authorId: String, dataToUpdate: FeedAuthorDataToUpdate) {
        TODO("Not yet implemented")
    }
}