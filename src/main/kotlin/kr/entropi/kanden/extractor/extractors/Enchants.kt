package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryOps
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.enchantment.Enchantment

class Enchants : Extractor.Extractor {
    override fun fileName(): String {
        return "enchants.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val enchantmentRegistry = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)

        val enchantsJson = JsonObject()

        for (key in enchantmentRegistry.registryKeySet()) {
            val enchantment = enchantmentRegistry.get(key).get().value()
            val body = Enchantment.DIRECT_CODEC.encodeStart(
                RegistryOps.create(
                    JsonOps.INSTANCE, server.registryAccess()
                ), enchantment
            ).getOrThrow()


            enchantsJson.add(key.identifier().toString(), body)
        }

        return enchantsJson
    }
}