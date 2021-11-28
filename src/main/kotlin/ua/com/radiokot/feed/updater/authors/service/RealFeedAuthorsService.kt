package ua.com.radiokot.feed.updater.authors.service

import ua.com.radiokot.feed.updater.authors.model.FeedAuthor
import ua.com.radiokot.feed.updater.authors.model.FeedAuthorDataToUpdate
import ua.com.radiokot.feed.updater.authors.model.FeedSite
import java.util.logging.Level
import java.util.logging.Logger
import javax.sql.DataSource

class RealFeedAuthorsService(
    private val dataSource: DataSource
) : FeedAuthorsService {
    override fun getAuthors(site: FeedSite?): List<FeedAuthor> {
        Logger.getGlobal()
            .log(
                Level.INFO, "get: " +
                        "site=$site"
            )

        var query = "SELECT * FROM author "
        if (site != null) {
            query += "WHERE siteId=? "
        }

        dataSource.connection.use { connection ->
            val preparedStatement = connection
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
    }

    override fun updateAuthorData(authorId: String, dataToUpdate: FeedAuthorDataToUpdate) {
        TODO("Not yet implemented")
    }
}