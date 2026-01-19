package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.core.registries.Registries
import net.minecraft.server.MinecraftServer

class DataComponents : Extractor.Extractor {
    override fun fileName() = "data_components.json"

    override fun extract(server: MinecraftServer): JsonElement {
        val json = JsonObject()
        json.add("types", types(server))

        return json
    }

    fun types(server: MinecraftServer): JsonObject {
        val registry = server.registryAccess().lookupOrThrow(Registries.DATA_COMPONENT_TYPE)
        val json = JsonObject()

        for (type in registry) {
            val id = registry.getId(type)
            val key = registry.getKey(type)!!

            val typeJson = JsonObject()
            typeJson.addProperty("id", id)
            typeJson.addProperty("name", key.path)
            typeJson.addProperty("ignoreSwapAnimation", type.ignoreSwapAnimation())
            typeJson.addProperty("isTransient", type.isTransient)

            json.add(key.toString(), typeJson)
        }

        return json
    }
}