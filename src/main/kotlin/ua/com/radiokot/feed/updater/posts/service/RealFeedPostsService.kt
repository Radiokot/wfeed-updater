package ua.com.radiokot.feed.updater.posts.service

import mu.KotlinLogging
import ua.com.radiokot.feed.updater.authors.model.FeedSite
import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import java.sql.PreparedStatement
import java.sql.Statement
import java.util.*
import javax.sql.DataSource

class RealFeedPostsService(
    private val dataSource: DataSource
) : FeedPostsService {
    private val logger = KotlinLogging.logger("RealFeedPostsService")

    override fun savePosts(posts: List<FeedPostToSave>) {
        logger.debug {
            "save: " +
                    "posts=${posts.size}"
        }

        dataSource.connection.use { connection ->
            connection.autoCommit = false

            var newPostsCount = 0
            var newAttsCount = 0

            posts.chunked(5).forEach { postsChunk ->
                val postsInsertStatement = connection.prepareStatement(
                    "INSERT IGNORE INTO `post`(\n" +
                            "    `id`,\n" +
                            "    `api_id`,\n" +
                            "    `author_id`,\n" +
                            "    `text`,\n" +
                            "    `date`,\n" +
                            "    `url`\n" +
                            ")\n" +
                            "VALUES(?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                )

                val attsInsertStatement = connection.prepareStatement(
                    "INSERT IGNORE INTO `att`(\n" +
                            "    `post_id`,\n" +
                            "    `i`,\n" +
                            "    `type`,\n" +
                            "    `api_id`,\n" +
                            "    `photo_height`,\n" +
                            "    `photo_width`,\n" +
                            "    `photo_130`,\n" +
                            "    `photo_604`,\n" +
                            "    `photo_807`,\n" +
                            "    `photo_1280`,\n" +
                            "    `photo_2560`\n" +
                            ")\n" +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?)"
                )

                postsChunk.forEach { post ->
                    postsInsertStatement.apply {
                        var i = 0
                        setString(++i, post.id)
                        setString(++i, post.apiId)
                        setInt(++i, post.author.id)
                        setString(++i, post.text)
                        setTimestamp(++i, post.sqlTimestamp)
                        setString(++i, post.url)
                    }
                    postsInsertStatement.addBatch()

                    post.attachments.forEachIndexed { attachmentI, attachment ->
                        attsInsertStatement.apply {
                            var i = 0
                            setString(++i, post.id)
                            setInt(++i, attachmentI)
                            setString(++i, attachment.type)
                            setString(++i, attachment.apiId)

                            check(
                                when (attachment) {
                                    is FeedPostToSave.Attachment.Photo -> {
                                        setInt(++i, attachment.height)
                                        setInt(++i, attachment.width)
                                        setString(++i, attachment.url130)
                                        setString(++i, attachment.url604)
                                        setString(++i, attachment.url807)
                                        setString(++i, attachment.url1280)
                                        setString(++i, attachment.url2560)
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
                    newPostsCount += postsInsertStatement.use(PreparedStatement::executeBatch).sum()
                    newAttsCount += attsInsertStatement.use(PreparedStatement::executeBatch).sum()
                    connection.commit()

                    logger.debug {
                        "saved_posts_chunk: " +
                                "chunk=${postsChunk.size}"
                    }
                } catch (e: Exception) {
                    logger.error {
                        "save_posts_chunk_error: " +
                                "error=$e,\n" +
                                "chunk=$postsChunk,\n" +
                                "postsInsert=$postsInsertStatement,\n" +
                                "attsInsert=$attsInsertStatement"
                    }

                    connection.rollback()

                    throw e
                }
            }


            logger.info {
                "saved_posts: " +
                        "newPostsCount=$newPostsCount, " +
                        "newAttsCount=$newAttsCount"
            }
        }
    }

    override fun getLastPostApiId(site: FeedSite): String? {
        logger.debug {
            "get_last_post_api_id: " +
                    "site=$site"
        }

        return dataSource.connection.use { connection ->
            val preparedStatement = connection.prepareStatement(
                "SELECT post.api_id FROM post, author " +
                        "WHERE post.author_id=author.id " +
                        "AND author.site_id=? " +
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
                            logger.debug {
                                "got_last_post_api_id: " +
                                        "site=$site, " +
                                        "apiId=$it"
                            }
                        }
                }
            }
        }
    }

    override fun getLastPostDate(site: FeedSite): Date? {
        logger.debug {
            "get_last_post_date: " +
                    "site=$site"
        }

        return dataSource.connection.use { connection ->
            val preparedStatement = connection.prepareStatement(
                "SELECT post.date FROM post, author " +
                        "WHERE post.author_id=author.id " +
                        "AND author.site_id=? " +
                        "ORDER BY post.date " +
                        "DESC LIMIT 1"
            ).apply {
                setInt(1, site.i)
            }

            preparedStatement.use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet
                        .takeIf { it.next() }
                        ?.let { Date(it.getTimestamp(1).time) }
                        .also {
                            logger.debug {
                                "got_last_post_date: " +
                                        "site=$site, " +
                                        "date=$it"
                            }
                        }
                }
            }
        }
    }
}