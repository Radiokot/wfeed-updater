package ua.com.radiokot.feed.updater

import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import ua.com.radiokot.feed.updater.posts.model.FeedPostToSave
import ua.com.radiokot.feed.updater.di.injectionModules
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.TumblrDashboardService
import ua.com.radiokot.feed.updater.vk.walls.service.VkWallsService

@KoinApiExtension
object Application : KoinComponent {
    private val tumblrDashboardService: TumblrDashboardService by inject()
    private val vkWallsService: VkWallsService by inject()

    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            printLogger()

            fileProperties("/keystore.properties")
            fileProperties("/vk_proxy.properties")
            environmentProperties()

            modules(injectionModules)
        }

        vkWallsService
            .getGroupWalls(
                groupIds = setOf("33376933", "35486596"),
                wallPostsLimit = 1
            )
            .forEach {
                println(it)
                it.posts.forEach { post ->
                    println(FeedPostToSave(post))
                }
            }
    }
}