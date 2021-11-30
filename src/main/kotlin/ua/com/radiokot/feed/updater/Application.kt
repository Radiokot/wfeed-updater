package ua.com.radiokot.feed.updater

import mu.KotlinLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import ua.com.radiokot.feed.updater.authors.model.FeedSite
import ua.com.radiokot.feed.updater.di.KLoggerKoinLogger
import ua.com.radiokot.feed.updater.di.injectionModules
import ua.com.radiokot.feed.updater.posts.service.FeedPostsService
import ua.com.radiokot.feed.updater.util.Running
import ua.com.radiokot.feed.updater.vk.VkUpdater
import java.time.Duration
import java.util.*

@KoinApiExtension
object Application : KoinComponent {
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
                val minVkStartDate =
                    Date(System.currentTimeMillis() - Duration.ofDays(7).toMillis())

                vkUpdater
                    .update(
                        startDate = lastVkPostDate
                            ?.coerceAtLeast(minVkStartDate)
                            ?: minVkStartDate
                    )
            },
            runnableName = "VkUpdater",
            normalInterval = Duration.ofMinutes(3),
            minAbnormalInterval = Duration.ofMinutes(3),
            maxAbnormalInterval = Duration.ofMinutes(15),
        )
    }
}