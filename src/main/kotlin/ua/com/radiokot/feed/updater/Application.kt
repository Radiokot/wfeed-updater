package ua.com.radiokot.feed.updater

import mu.KotlinLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import ua.com.radiokot.feed.updater.authors.model.FeedSite
import ua.com.radiokot.feed.updater.authors.service.FeedAuthorsService
import ua.com.radiokot.feed.updater.di.KLoggerKoinLogger
import ua.com.radiokot.feed.updater.di.injectionModules
import ua.com.radiokot.feed.updater.posts.service.FeedPostsService
import ua.com.radiokot.feed.updater.util.Running
import ua.com.radiokot.feed.updater.vk.VkUpdater
import java.time.Duration

@KoinApiExtension
object Application : KoinComponent {
    private val feedAuthorsService: FeedAuthorsService by inject()
    private val feedPostsService: FeedPostsService by inject()
    private val vkUpdater: VkUpdater by inject()

    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            logger(KLoggerKoinLogger(KotlinLogging.logger("Koin")))

            fileProperties("/keystore.properties")
            fileProperties("/vk_proxy.properties")
            fileProperties("/database.properties")
            environmentProperties()

            modules(injectionModules)
        }

        Running.withBackoff(
            runnable = {
                val lastVkPostDate = feedPostsService.getLastPostDate(FeedSite.VK)
                val minVkFeedStartTimeUnix = System.currentTimeMillis() / 1000L - 3600 * 24
                val vkFeedStartTimeUnix =
                    if (lastVkPostDate != null)
                        (lastVkPostDate.time / 1000L).coerceAtLeast(minVkFeedStartTimeUnix)
                    else
                        minVkFeedStartTimeUnix

                vkUpdater
                    .update(
                        feedAuthors = feedAuthorsService.getAuthors(FeedSite.VK),
                        startTimeUnix = vkFeedStartTimeUnix
                    )
            },
            runnableName = "VkUpdater",
            normalInterval = Duration.ofMinutes(3),
            minAbnormalInterval = Duration.ofMinutes(3),
            maxAbnormalInterval = Duration.ofMinutes(15),
        )
    }
}