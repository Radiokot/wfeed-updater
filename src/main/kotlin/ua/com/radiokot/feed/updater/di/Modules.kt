package ua.com.radiokot.feed.updater.di

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.dbcp2.BasicDataSource
import org.flywaydb.core.Flyway
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ua.com.radiokot.feed.updater.authors.service.FeedAuthorsService
import ua.com.radiokot.feed.updater.authors.service.RealFeedAuthorsService
import ua.com.radiokot.feed.updater.extensions.getNotEmptyProperty
import ua.com.radiokot.feed.updater.posts.service.FeedPostsService
import ua.com.radiokot.feed.updater.posts.service.RealFeedPostsService
import ua.com.radiokot.feed.updater.tumblr.TumblrUpdater
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.TumblrDashboardService
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.real.RealTumblrDashboardService
import ua.com.radiokot.feed.updater.tumblr.util.oauth1.OAuth1Keys
import ua.com.radiokot.feed.updater.tumblr.util.oauth1.OAuth1SigningInterceptor
import ua.com.radiokot.feed.updater.util.http.HttpExceptionInterceptor
import ua.com.radiokot.feed.updater.vk.VkUpdater
import ua.com.radiokot.feed.updater.vk.util.OAuth2TokenInterceptor
import ua.com.radiokot.feed.updater.vk.util.VkApiProxyPrefixInterceptor
import ua.com.radiokot.feed.updater.vk.walls.service.RealVkNewsfeedService
import ua.com.radiokot.feed.updater.vk.walls.service.RealVkWallsService
import ua.com.radiokot.feed.updater.vk.walls.service.VkNewsfeedService
import ua.com.radiokot.feed.updater.vk.walls.service.VkWallsService
import java.time.Duration
import javax.sql.DataSource

val injectionModules: List<Module> = listOf(
    // JSON
    module {
        single<ObjectMapper> {
            jacksonObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    },

    // HTTP clients
    module {
        fun getLoggingInterceptor(): HttpLoggingInterceptor {
            val logger = KotlinLogging.logger("HTTP")
            return HttpLoggingInterceptor(logger::info).apply {
                level =
                    if (logger.isDebugEnabled)
                        HttpLoggingInterceptor.Level.BODY
                    else
                        HttpLoggingInterceptor.Level.BASIC
            }
        }

        fun getDefaultBuilder(): OkHttpClient.Builder {
            return OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .addInterceptor(HttpExceptionInterceptor())
        }

        // Tumblr
        single(named(InjectedHttpClient.TUMBLR)) {
           getDefaultBuilder()
                .addInterceptor(OAuth1SigningInterceptor {
                    OAuth1Keys(
                        consumerKey = getNotEmptyProperty("TUMBLR_CONSUMER_KEY"),
                        consumerSecret = getNotEmptyProperty("TUMBLR_CONSUMER_SECRET"),
                        accessToken = getNotEmptyProperty("TUMBLR_ACCESS_TOKEN"),
                        accessSecret = getNotEmptyProperty("TUMBLR_ACCESS_SECRET")
                    )
                })
                .addInterceptor(getLoggingInterceptor())
                .build()
        }

        // VK with all the tokens
        single(named(InjectedHttpClient.VK)) {
            getDefaultBuilder()
                .addInterceptor(
                    OAuth2TokenInterceptor(
                        accessToken = getNotEmptyProperty("VK_ACCESS_TOKEN")
                    )
                )
                .apply {
                    val proxyUrl = getPropertyOrNull("VK_API_PROXY_URL")
                        ?.takeIf(String::isNotEmpty)

                    if (proxyUrl != null) {
                        addInterceptor(
                            VkApiProxyPrefixInterceptor(
                                proxyUrl = proxyUrl
                            )
                        )
                    }
                }
                .addInterceptor(getLoggingInterceptor())
                .build()
        }
    },

    // Services
    module {
        single<TumblrDashboardService> {
            RealTumblrDashboardService(
                tumblrHttpClient = get(named(InjectedHttpClient.TUMBLR)),
                mapper = get()
            )
        }

        single<VkWallsService> {
            RealVkWallsService(
                vkHttpClient = get(named(InjectedHttpClient.VK)),
                mapper = get()
            )
        }

        single<VkNewsfeedService> {
            RealVkNewsfeedService(
                vkHttpClient = get(named(InjectedHttpClient.VK)),
                mapper = get()
            )
        }

        single<FeedAuthorsService> {
            RealFeedAuthorsService(
                dataSource = get()
            )
        }

        single<FeedPostsService> {
            RealFeedPostsService(
                dataSource = get()
            )
        }
    },

    // Database
    module {
        single<DataSource> {
            val dbName = getNotEmptyProperty("DB_NAME")
            val dbHost = getNotEmptyProperty("DB_HOST")
            val dbPort = getNotEmptyProperty("DB_PORT")
            val dbUser = getNotEmptyProperty("DB_USER")
            val dbPassword = getNotEmptyProperty("DB_PASSWORD")

            BasicDataSource().apply {
                url = "jdbc:mysql://$dbHost:$dbPort/$dbName" +
                        "?useSSL=false" +
                        "&useUnicode=yes" +
                        "&character_set_server=utf8mb4" +
                        "characterEncoding=UTF-8"
                username = dbUser
                password = dbPassword

                minIdle = 3
                maxIdle = 9

                Flyway
                    .configure()
                    .dataSource(this)
                    .load()
                    .migrate()
            }
        }
    },

    // Updaters
    module {
        single {
            VkUpdater(
                vkNewsfeedService = get(),
                feedAuthorsService = get(),
                feedPostsService = get(),
                vkPhotoProxyUrl = getPropertyOrNull("VK_PHOTO_PROXY_URL")
                    ?.takeIf(String::isNotEmpty)
            )
        }

        single {
            TumblrUpdater(
                tumblrDashboardService = get(),
                feedAuthorsService = get(),
                feedPostsService = get(),
            )
        }
    },
)
