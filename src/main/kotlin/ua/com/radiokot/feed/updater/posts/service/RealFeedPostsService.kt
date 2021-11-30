package ua.com.radiokot.feed.updater.posts.service

import ua.com.radiokot.feed.updater.authors.model.FeedSite
import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import java.sql.PreparedStatement
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.sql.DataSource

class RealFeedPostsService(
    private val dataSource: DataSource
) : FeedPostsService {

    override fun savePosts(posts: List<FeedPostToSave>) {
        Logger.getGlobal()
            .log(
                Level.INFO, "save: " +
                        "posts=${posts.size}"
            )

        dataSource.connection.use { connection ->
            connection.autoCommit = false

            posts.chunked(5).forEach { postsChunk ->
                Logger.getGlobal()
                    .log(
                        Level.INFO, "save_chunk: " +
                                "chunk=${postsChunk.size}"
                    )

                val postsInsertStatement = connection.prepareStatement(
                    "INSERT IGNORE INTO post (id, apiId, authorId, text, date, url) VALUES (?,?,?,?,?,?)"
                )

                val attsInsertStatement = connection.prepareStatement(
                    "INSERT IGNORE INTO atts(uniqId, id, type, VkAttId, photoHeight, photoWidth," +
                            " photo130, photo604, photo807, photo1280, photo2560) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?)"
                )

                postsChunk.forEach { post ->
                    postsInsertStatement.apply {
                        var i = 0
                        setString(++i, post.id)
                        setString(++i, post.apiId)
                        setInt(++i, post.author.id)
                        setString(++i, post.text)
                        setLong(++i, post.timestamp)
                        setString(++i, post.url)
                    }
                    postsInsertStatement.addBatch()

                    post.attachments.forEachIndexed{ attachmentI, attachment ->
                        attsInsertStatement.apply {
                            var i = 0
                            setString(++i, "${post.id}_$attachmentI")
                            setString(++i, post.id)
                            setString(++i, attachment.type)

                            check(
                                when (attachment) {
                                    is FeedPostToSave.Attachment.Photo -> {
                                        setString(++i, attachment.vkId)
                                        setInt(++i, attachment.height)
                                        setInt(++i, attachment.width)
                                        setString(++i, attachment.url130 ?: "0")
                                        setString(++i, attachment.url604 ?: "0")
                                        setString(++i, attachment.url807 ?: "0")
                                        setString(++i, attachment.url1280 ?: "0")
                                        setString(++i, attachment.url2560 ?: "0")
                                        true
                                    }
                                }
                            )
                            addBatch()
                        }
                    }
                }

                try {
                    connection.commit()
                    postsInsertStatement.use(PreparedStatement::executeBatch)
                    attsInsertStatement.use(PreparedStatement::executeBatch)
                    connection.commit()
                } catch (e: Exception) {
                    Logger.getGlobal()
                        .log(
                            Level.SEVERE, "save_posts_chunk_error: " +
                                    "error=$e,\n" +
                                    "chunk=$postsChunk"
                        )

                    connection.rollback()

                    throw e
                }
            }
        }
    }

    override fun getLastPostApiId(site: FeedSite): String? {
        Logger.getGlobal()
            .log(
                Level.INFO, "get: " +
                        "site=$site"
            )

        return dataSource.connection.use { connection ->
            val preparedStatement = connection.prepareStatement(
                "SELECT post.apiId FROM post, author " +
                        "WHERE post.authorId=author.id " +
                        "AND author.siteId=? " +
                        "ORDER BY post.date " +
                        "DESC LIMIT 1"
            ).apply {
                setInt(1, site.i)
            }

            preparedStatement.use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet
                        .takeIf { it.next() }
                        ?.getString(1)
                        .also {
                            Logger.getGlobal()
                                .log(
                                    Level.INFO, "got: " +
                                            "apiId=$it"
                                )
                        }
                }
            }
        }
    }

    override fun getLastPostDate(site: FeedSite): Date? {
        Logger.getGlobal()
            .log(
                Level.INFO, "get: " +
                        "site=$site"
            )

        return dataSource.connection.use { connection ->
            val preparedStatement = connection.prepareStatement(
                "SELECT post.date FROM post, author " +
                        "WHERE post.authorId=author.id " +
                        "AND author.siteId=? " +
                        "ORDER BY post.date " +
                        "DESC LIMIT 1"
            ).apply {
                setInt(1, site.i)
            }

            preparedStatement.use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet
                        .takeIf { it.next() }
                        ?.let { Date(it.getLong(1) * 1000) }
                        .also {
                            Logger.getGlobal()
                                .log(
                                    Level.INFO, "got: " +
                                            "date=$it"
                                )
                        }
                }
            }
        }
    }
}