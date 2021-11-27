package ua.com.radiokot.feed.updater.extensions

import okhttp3.HttpUrl

fun HttpUrl.Builder.addNotNullQueryParameter(name: String, value: Any?) =
    value
        ?.let { addQueryParameter(name, it.toString()) }
        ?: this