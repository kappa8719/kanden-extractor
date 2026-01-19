package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.JsonOps
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryOps
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.Item


class Items : Extractor.Extractor {
    fun enchantable(item: Item): Int? {
        return item.components().get(DataComponents.ENCHANTABLE)?.value
    }

    fun damageResistant(item: Item): JsonElement {
        val component = item.components().get(DataComponents.DAMAGE_RESISTANT)
            ?: return JsonNull.INSTANCE
        return JsonPrimitive(component.types.location.path)
    }


    override fun fileName(): String {
        return "items.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val itemRegistry = server.registryAccess().lookupOrThrow(Registries.ITEM)

        val itemsJson = JsonArray()

        for (key in itemRegistry.registryKeySet()) {
            val itemHolder = itemRegistry.get(key).get()
            val item = itemHolder.value()

            val itemJson = JsonObject()

            itemJson.addProperty("id", itemRegistry.getId(item))
            itemJson.addProperty("name", key.identifier().path)
            itemJson.addProperty("translation_key", item.descriptionId)
            itemJson.addProperty("max_stack", item.defaultMaxStackSize)
            itemJson.addProperty("max_durability", item.defaultInstance.maxDamage)
            itemJson.addProperty("enchantable", enchantable(item))
            itemJson.add("damage_resistant", damageResistant(item))

            itemJson.add(
                "components",
                DataComponentMap.CODEC.encodeStart(
                    RegistryOps.create(
                        JsonOps.INSTANCE,
                        server.registryAccess()
                    ), item.components()
                ).getOrThrow()
            )

            itemsJson.add(itemJson)
        }
        return itemsJson
    }
}