package ua.com.radiokot.feed.updater.posts.service

import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import java.sql.PreparedStatement
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
                    "INSERT IGNORE INTO `atts`(`id`, `type`, `VkAttId`, `photoHeight`, `photoWidth`," +
                            " `photo130`, `photo604`, `photo807`, `photo1280`, `photo2560`) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?)"
                )

                postsChunk.forEach { post ->
                    postsInsertStatement.apply {
                        var i = 0
                        setString(++i, post.id)
                        setString(++i, post.apiId)
                        setInt(++i, post.author.id.toInt())
                        setString(++i, post.text)
                        setLong(++i, post.timestamp)
                        setString(++i, post.url)
                    }
                    postsInsertStatement.addBatch()

                    post.attachments.forEach { attachment ->
                        attsInsertStatement.apply {
                            var i = 0
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
                }
            }
        }
    }
}