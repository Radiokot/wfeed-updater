package ua.com.radiokot.feed.updater.authors.service

import ua.com.radiokot.feed.updater.authors.model.FeedAuthor
import ua.com.radiokot.feed.updater.authors.model.FeedAuthorDataToUpdate
import ua.com.radiokot.feed.updater.authors.model.FeedSite

interface FeedAuthorsService {
    fun getAuthors(site: FeedSite?): List<FeedAuthor>

    fun updateAuthorData(
        authorId: String,
        dataToUpdate: FeedAuthorDataToUpdate
    )
}