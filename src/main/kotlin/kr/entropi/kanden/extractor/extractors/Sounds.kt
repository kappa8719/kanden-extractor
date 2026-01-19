package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.MinecraftServer

class Sounds : Extractor.Extractor {
    override fun fileName(): String {
        return "sounds.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val itemsJson = JsonArray()

        for (sound in BuiltInRegistries.SOUND_EVENT) {
            val itemJson = JsonObject()
            itemJson.addProperty("id", BuiltInRegistries.SOUND_EVENT.getId(sound))
            itemJson.addProperty("name", BuiltInRegistries.SOUND_EVENT.getKey(sound)!!.path)
            itemsJson.add(itemJson)
        }

        return itemsJson
    }
}