package ua.com.radiokot.feed.updater.tumblr.util.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.util.*
import java.util.concurrent.TimeUnit

class UnixTimestampDateDeserializer : JsonDeserializer<Date>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Date? {
        val timestamp = p.valueAsString ?: return null
        return Date(TimeUnit.SECONDS.toMillis(timestamp.toLong()))
    }
}