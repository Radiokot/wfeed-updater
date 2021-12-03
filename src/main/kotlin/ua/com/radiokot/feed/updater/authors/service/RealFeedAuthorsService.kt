package ua.com.radiokot.feed.updater.authors.service

import mu.KotlinLogging
import ua.com.radiokot.feed.updater.authors.model.FeedAuthor
import ua.com.radiokot.feed.updater.authors.model.FeedAuthorDataToUpdate
import ua.com.radiokot.feed.updater.authors.model.FeedSite
import javax.sql.DataSource

class RealFeedAuthorsService(
    private val dataSource: DataSource
) : FeedAuthorsService {
    private val logger = KotlinLogging.logger("RealFeedAuthorsService")

    override fun getAuthors(site: FeedSite?): List<FeedAuthor> {
        logger.debug {
            "get_authors: " +
                    "site=$site"
        }

        var query = "SELECT * FROM author "
        if (site != null) {
            query += "WHERE site_id=? "
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
                    }.toList().also {
                        logger.debug {
                            "got_authors: " +
                                    "site=$site, " +
                                    "authors=${it.size}"
                        }
                    }
                }
            }
        }
    }

    override fun updateAuthorData(
        authorId: Int,
        dataToUpdate: FeedAuthorDataToUpdate
    ) {
        logger.debug {
            "update_data: " +
                    "authorId=$authorId"
        }

        dataSource.connection.use { connection ->
            connection.autoCommit = true

            val preparedStatement = connection.prepareStatement(
                "UPDATE author SET name=?, photo=? WHERE id=?"
            ).apply {
                var i = 0
                setString(++i, dataToUpdate.name)
                setString(++i, dataToUpdate.photoUrl)
                setInt(++i, authorId)
            }

            preparedStatement.use { statement ->
                try {
                    statement.executeUpdate()

                    logger.debug {
                        "data_updated: " +
                                "authorId=$authorId, " +
                                "data=$dataToUpdate"
                    }
                } catch (e: Exception) {
                    logger.error {
                        "update_data_error: " +
                                "error=$e,\n" +
                                "authorId=$authorId, " +
                                "data=$dataToUpdate"
                    }

                    throw e
                }
            }
        }
    }
}