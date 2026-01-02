package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryOps
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.decoration.painting.PaintingVariant

class Paintings : Extractor.Extractor {
    override fun fileName(): String {
        return "paintings.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val registry = server.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT)
        val codec = PaintingVariant.DIRECT_CODEC

        val json = JsonObject()
        registry.registryKeySet().forEach { key ->
            val valueRef = registry.getOrThrow(key)
            val value = valueRef.value()
            json.add(
                key.identifier().toString(),
                codec.encodeStart(RegistryOps.create(JsonOps.INSTANCE, server.registryAccess()), value).getOrThrow()
            )
        }

        return json
    }
}