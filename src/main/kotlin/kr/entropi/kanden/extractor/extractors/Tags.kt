package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import kr.entropi.kanden.extractor.RegistryKeyComparator
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySynchronization
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import java.util.*
import kotlin.streams.asSequence

class Tags : Extractor.Extractor {
    override fun fileName(): String {
        return "tags.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val tagsJson = JsonObject()

        val registryTags =
            RegistrySynchronization.networkSafeRegistries(server.registries()).asSequence()
                .map { it.key() to serializeTags(it.value()) }
                .filter { !it.second.isEmpty() }
                .fold(TreeMap<ResourceKey<*>, Map<Identifier, JsonArray>>(RegistryKeyComparator)) { acc, pair ->
                    acc.apply { put(pair.first, pair.second) }
                }

        for (registry in registryTags.entries) {
            val tagGroupTagsJson = JsonObject().apply {
                registry.value.entries.forEach { tag ->
                    add(tag.key.toString(), tag.value)
                }
            }

            tagsJson.add(registry.key.identifier().toString(), tagGroupTagsJson)
        }

        return tagsJson
    }

    private fun <T : Any> serializeTags(registry: Registry<T>): MutableMap<Identifier, JsonArray> {
        val map = TreeMap<Identifier, JsonArray>()
        registry.tags.forEach { it ->
            val intList = JsonArray(it.size())
            for (holder in it) {
                intList.add(registry.getId(holder.value()))
            }
            map.put(it.key().location, intList)
        }
        return map
    }
}