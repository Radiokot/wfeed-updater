package ua.com.radiokot.feed.updater.authors.model

enum class FeedSite(val i: Int) {
    INTERNAL(0),
    VK(1),
    TUMBLR(2),
    ;

    companion object {
        fun valueOf(i: Int) =
            values().first { it.i == i }
    }
}