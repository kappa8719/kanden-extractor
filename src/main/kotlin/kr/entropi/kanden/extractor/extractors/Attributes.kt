package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer

class Attributes: Extractor.Extractor {
    override fun fileName(): String {
        return "attributes.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val attributesJson = JsonObject()

        for (attribute in Registries.ATTRIBUTE) {
            val attributeJson = JsonObject()

            attributeJson.addProperty("id", Registries.ATTRIBUTE.getRawId(attribute))
            attributeJson.addProperty("name", Registries.ATTRIBUTE.getId(attribute)!!.getPath())
            attributeJson.addProperty("default_value", attribute.getDefaultValue())
            attributeJson.addProperty("translation_key", attribute.getTranslationKey())
            attributeJson.addProperty("tracked", attribute.isTracked())

            if (attribute is ClampedEntityAttribute) {
                attributeJson.addProperty("min_value", attribute.minValue)
                attributeJson.addProperty("max_value", attribute.maxValue)
            }

            attributesJson.add(Registries.ATTRIBUTE.getId(attribute)!!.path, attributeJson)
        }

        return attributesJson
    }
}