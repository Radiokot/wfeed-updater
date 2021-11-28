package ua.com.radiokot.feed.updater.di

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.dbcp2.BasicDataSource
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ua.com.radiokot.feed.updater.authors.service.FeedAuthorsService
import ua.com.radiokot.feed.updater.authors.service.RealFeedAuthorsService
import ua.com.radiokot.feed.updater.extensions.getNotEmptyProperty
import ua.com.radiokot.feed.updater.posts.service.FeedPostsService
import ua.com.radiokot.feed.updater.posts.service.RealFeedPostsService
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.TumblrDashboardService
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.real.RealTumblrDashboardService
import ua.com.radiokot.feed.updater.tumblr.util.oauth1.OAuth1Keys
import ua.com.radiokot.feed.updater.tumblr.util.oauth1.OAuth1SigningInterceptor
import ua.com.radiokot.feed.updater.vk.util.OAuth2MultipleTokensInterceptor
import ua.com.radiokot.feed.updater.vk.util.VkApiProxyPrefixInterceptor
import ua.com.radiokot.feed.updater.vk.walls.service.VkNewsfeedService
import ua.com.radiokot.feed.updater.vk.walls.service.VkWallsService
import ua.com.radiokot.feed.updater.vk.walls.service.real.RealVkNewsfeedService
import ua.com.radiokot.feed.updater.vk.walls.service.real.RealVkWallsService
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
        factory {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        }

        // Tumblr
        single(named(InjectedHttpClient.TUMBLR)) {
            OkHttpClient.Builder()
                .addInterceptor(OAuth1SigningInterceptor {
                    OAuth1Keys(
                        consumerKey = getNotEmptyProperty("TUMBLR_CONSUMER_KEY"),
                        consumerSecret = getNotEmptyProperty("TUMBLR_CONSUMER_SECRET"),
                        accessToken = getNotEmptyProperty("TUMBLR_ACCESS_TOKEN"),
                        accessSecret = getNotEmptyProperty("TUMBLR_ACCESS_SECRET")
                    )
                })
                .addInterceptor(get<HttpLoggingInterceptor>())
                .build()
        }

        // VK with access tokens as a param
        factory(named(InjectedHttpClient.VK_WITH_PARAMS)) { params ->
            OkHttpClient.Builder()
                .addInterceptor(
                    OAuth2MultipleTokensInterceptor(
                        accessTokens = params[0]
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
                .addInterceptor(get<HttpLoggingInterceptor>())
                .build()
        }

        // VK with all the tokens
        single(named(InjectedHttpClient.VK)) {
            val tokens = getNotEmptyProperty("VK_ACCESS_TOKENS")
                .split(',')
                .map(String::trim)

            get<OkHttpClient>(named(InjectedHttpClient.VK_WITH_PARAMS)) { parametersOf(tokens) }
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
                        "&characterEncoding=utf8"
                username = dbUser
                password = dbPassword

                minIdle = 3
                maxIdle = 9
            }
        }
    },
)