package ua.com.radiokot.feed.updater.extensions

import org.koin.core.scope.Scope

fun Scope.getNotEmptyProperty(key: String) =
    getProperty(key)
        .also { check(it.isNotEmpty()) { "Property '$key' must not be empty" } }