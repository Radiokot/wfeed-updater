package ua.com.radiokot.feed.updater.di

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ua.com.radiokot.feed.updater.extensions.getNotEmptyProperty
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.TumblrDashboardService
import ua.com.radiokot.feed.updater.tumblr.dashboard.service.real.RealTumblrDashboardService
import ua.com.radiokot.feed.updater.tumblr.util.oauth1.Oauth1SigningInterceptor
import ua.com.radiokot.feed.updater.tumblr.util.oauth1.OauthKeys

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
                level = HttpLoggingInterceptor.Level.BODY
            }
        }

        single(named(InjectedHttpClient.TUMBLR)) {
            OkHttpClient.Builder()
                .addInterceptor(Oauth1SigningInterceptor({
                    OauthKeys(
                        consumerKey = getNotEmptyProperty("TUMBLR_CONSUMER_KEY"),
                        consumerSecret = getNotEmptyProperty("TUMBLR_CONSUMER_SECRET"),
                        accessToken = getNotEmptyProperty("TUMBLR_ACCESS_TOKEN"),
                        accessSecret = getNotEmptyProperty("TUMBLR_ACCESS_SECRET")
                    )
                }))
                .addInterceptor(get<HttpLoggingInterceptor>())
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
    },
)