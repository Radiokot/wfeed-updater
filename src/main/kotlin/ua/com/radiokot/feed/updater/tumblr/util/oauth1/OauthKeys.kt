package ua.com.radiokot.feed.updater.tumblr.util.oauth1

data class OauthKeys(
    val consumerKey: String,
    val consumerSecret: String,
    val accessToken: String? = null,
    val accessSecret: String? = null
)