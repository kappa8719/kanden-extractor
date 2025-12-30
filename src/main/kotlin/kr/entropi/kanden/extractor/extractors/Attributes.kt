package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.ai.attributes.RangedAttribute

class Attributes : Extractor.Extractor {
    override fun fileName(): String {
        return "attributes.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val attributesJson = JsonObject()

        for (attribute in BuiltInRegistries.ATTRIBUTE) {
            val id = BuiltInRegistries.ATTRIBUTE.getId(attribute)
            val key = BuiltInRegistries.ATTRIBUTE.getKey(attribute)!!

            val attributeJson = JsonObject()

            attributeJson.addProperty("id", id)
            attributeJson.addProperty("name", key.path)
            attributeJson.addProperty("default_value", attribute.defaultValue)
            attributeJson.addProperty("translation_key", attribute.descriptionId)
            attribute.toString()
            attributeJson.addProperty("tracked", attribute.isClientSyncable)


            if (attribute is RangedAttribute) {
                attributeJson.addProperty("min_value", attribute.minValue)
                attributeJson.addProperty("max_value", attribute.maxValue)
            }

            attributesJson.add(key.path, attributeJson)
        }

        return attributesJson
    }
}