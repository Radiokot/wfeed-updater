package ua.com.radiokot.feed.updater

import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import ua.com.radiokot.feed.updater.di.injectionModules
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.TumblrDashboardService

@KoinApiExtension
object Application : KoinComponent {
    private val tumblrDashboardService: TumblrDashboardService by inject()

    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            printLogger()

            fileProperties("/keystore.properties")
            environmentProperties()

            modules(injectionModules)
        }

        tumblrDashboardService
            .getDashboardPosts(
                sinceId = "668952449469612032",
                type = "photo"
            )
            .forEach {
                println(it)
            }
    }
}