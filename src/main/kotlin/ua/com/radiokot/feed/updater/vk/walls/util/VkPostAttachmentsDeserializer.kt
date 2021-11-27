package ua.com.radiokot.feed.updater.vk.walls.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import ua.com.radiokot.feed.updater.vk.walls.model.VkPost

class VkPostAttachmentsDeserializer : JsonDeserializer<List<VkPost.Attachment>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<VkPost.Attachment> {
        val attachmentsArrayJson = ctxt.readTree(p)

        return attachmentsArrayJson
            .mapNotNull { attachmentJson ->
                when (attachmentJson["type"].asText()) {
                    VkPost.Attachment.Photo.TYPE ->
                        ctxt.readTreeAsValue(
                            attachmentJson["photo"],
                            VkPost.Attachment.Photo::class.java
                        )
                    else ->
                        null
                }
            }
    }
}