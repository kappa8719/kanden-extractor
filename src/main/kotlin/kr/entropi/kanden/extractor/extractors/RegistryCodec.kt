package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.core.LayeredRegistryAccess
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.RegistryDataLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.server.RegistryLayer

class RegistryCodec : Extractor.Extractor {
    override fun fileName(): String {
        return "registry_codec.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val registries = RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream()
        val json = JsonObject()
        registries.forEach { entry ->
            json.add(entry.key.identifier().toString(), mapJson(entry, server.registryAccess(), server.registries()))
        }
        return json
    }

    fun <T : Any> mapJson(
        registryEntry: RegistryDataLoader.RegistryData<T>,
        registryManager: RegistryAccess.Frozen,
        combinedRegistries: LayeredRegistryAccess<RegistryLayer>
    ): JsonObject {
        val codec = registryEntry.elementCodec()
        val registry = registryManager.lookupOrThrow(registryEntry.key())
        val json = JsonObject()
        registry.registryKeySet().forEach { key ->
            val value = registry.getOrThrow(key).value()
            val element = codec.encodeStart(
                combinedRegistries.compositeAccess().createSerializationContext(JsonOps.INSTANCE),
                value
            ).result().orElseThrow()

            json.add(
                key.identifier().toString(),
                element
            )
        }
        return json
    }
}