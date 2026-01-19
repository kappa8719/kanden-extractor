package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.locale.Language
import net.minecraft.server.MinecraftServer

class TranslationKeys : Extractor.Extractor {
    override fun fileName(): String {
        return "translation_keys.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val translationsJson = JsonArray()

        val translations = extractTranslations()
        for (translation in translations.entries) {
            val translationKey = translation.key
            val translationValue = translation.value

            val translationJson = JsonObject()
            translationJson.addProperty("key", translationKey)
            translationJson.addProperty("english_translation", translationValue)

            translationsJson.add(translationJson)
        }

        return translationsJson
    }

    private fun extractTranslations(): MutableMap<String, String> {
        val language: Language = Language.getInstance()

        val anonymousClass: Class<out Language?> = language.javaClass
        for (field in anonymousClass.declaredFields) {
            try {
                val fieldValue = field.get(language)
                if (fieldValue is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    return fieldValue as MutableMap<String, String>
                }
            } catch (e: IllegalAccessException) {
                throw RuntimeException(
                    "Failed reflection on field '$field' on class '$anonymousClass'",
                    e
                )
            }
        }

        throw RuntimeException("Did not find anonymous map under 'net.minecraft.util.Language.create()'")
    }
}