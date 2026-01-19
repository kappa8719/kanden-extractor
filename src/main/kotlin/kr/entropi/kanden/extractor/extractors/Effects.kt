package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import kr.entropi.kanden.extractor.pascalCase
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier


class Effects : Extractor.Extractor {
    override fun fileName(): String {
        return "effects.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val effectsJson = JsonArray()

        for (effect in BuiltInRegistries.MOB_EFFECT) {
            val id = BuiltInRegistries.MOB_EFFECT.getId(effect)
            val key = BuiltInRegistries.MOB_EFFECT.getKey(effect)!!

            val effectJson = JsonObject()

            effectJson.addProperty("id", id)
            effectJson.addProperty("name", key.path)
            effectJson.addProperty("translation_key", effect.descriptionId)
            effectJson.addProperty("color", effect.color)
            effectJson.addProperty("instant", effect.isInstantenous)
            effectJson.addProperty("category", effect.category.name.pascalCase())

            val attributeModifiersJson = JsonArray()

            effect.createModifiers(
                0
            ) { attribute: Holder<Attribute>, modifier: AttributeModifier? ->
                val attributeModifierJson = JsonObject()
                attributeModifierJson.addProperty("attribute", attribute.registeredName)
                attributeModifierJson.addProperty("operation", modifier!!.operation().id())
                attributeModifierJson.addProperty("base_value", modifier.amount)
                attributeModifierJson.addProperty("uuid", modifier.id().toLanguageKey())
                attributeModifiersJson.add(attributeModifierJson)
            }

            if (!attributeModifiersJson.isEmpty) {
                effectJson.add("attribute_modifiers", attributeModifiersJson)
            }

            effectsJson.add(effectJson)
        }

        return effectsJson
    }
}