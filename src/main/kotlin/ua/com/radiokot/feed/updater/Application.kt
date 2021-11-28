package ua.com.radiokot.feed.updater

import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import ua.com.radiokot.feed.updater.di.injectionModules
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.TumblrDashboardService
import ua.com.radiokot.feed.updater.vk.walls.service.VkNewsfeedService
import ua.com.radiokot.feed.updater.vk.walls.service.VkWallsService

@KoinApiExtension
object Application : KoinComponent {
    private val tumblrDashboardService: TumblrDashboardService by inject()
    private val vkWallsService: VkWallsService by inject()
    private val vkNewsfeedService: VkNewsfeedService by inject()

    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            printLogger()

            fileProperties("/keystore.properties")
            fileProperties("/vk_proxy.properties")
            environmentProperties()

            modules(injectionModules)
        }
//
//        tumblrDashboardService
//            .getDashboardPosts(
//                sinceId = "668952449469612032",
//                type = "photo"
//            )
//            .forEach {
//                println(it)
//                println(FeedPostToSave(it))
//            }
//        vkWallsService
//            .getGroupWalls(
//                groupIds = setOf("33376933", "35486596"),
//                wallPostsLimit = 1
//            )
//            .forEach {
//                println(it)
//                it.posts.forEach { post ->
//                    println(FeedPostToSave(post))
//                }
//            }

        vkNewsfeedService
            .getNewsfeed(
                count = 10,
            )
            .posts
            .forEach {
                println(it.date.toString() + " " + it.text)
            }
    }
}