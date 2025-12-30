package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.animal.sniffer.Sniffer
import java.lang.reflect.Modifier

class Misc : Extractor.Extractor {
    override fun fileName(): String {
        return "misc.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val miscJson = JsonObject()

        val entityTypeJson = JsonObject()
        for (type in BuiltInRegistries.ENTITY_TYPE) {
            entityTypeJson.addProperty(
                BuiltInRegistries.ENTITY_TYPE.getKey(type).path,
                BuiltInRegistries.ENTITY_TYPE.getId(type)
            )
        }
        miscJson.add("entity_type", entityTypeJson)

        val entityStatusJson = JsonObject()
        for (field in EntityEvent::class.java.declaredFields) {
            if (field.canAccess(null)) {
                val code = field.get(null) as? Byte ?: continue
                entityStatusJson.addProperty(field.name.lowercase(), code)
            }
        }
        miscJson.add("entity_status", entityStatusJson)

        val entityAnimationJson = JsonObject()
        for (field in ClientboundAnimatePacket::class.java.declaredFields) {
            field.isAccessible = true
            if (Modifier.isStatic(field.modifiers) && field.canAccess(null)
            ) {
                val i = field.get(null) as? Int ?: continue
                entityAnimationJson.addProperty(field.name.lowercase(), i)
            }
        }
        miscJson.add("entity_animation", entityAnimationJson)

        val villagerTypeJson = JsonObject()
        for (type in BuiltInRegistries.VILLAGER_TYPE) {
            villagerTypeJson.addProperty(
                BuiltInRegistries.VILLAGER_TYPE.getKey(type).path,
                BuiltInRegistries.VILLAGER_TYPE.getId(type)
            )
        }
        miscJson.add("villager_type", villagerTypeJson)

        val villagerProfessionJson = JsonObject()
        for (profession in BuiltInRegistries.VILLAGER_PROFESSION) {
            val key = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession)
            val id = BuiltInRegistries.VILLAGER_PROFESSION.getId(profession)
            villagerProfessionJson.addProperty(key.path, id as Number)
        }
        miscJson.add("villager_profession", villagerProfessionJson)

        val catVariantRegistry = server.registryAccess().lookupOrThrow(Registries.CAT_VARIANT)
        val catVariantJson = JsonObject()
        for (variant in catVariantRegistry) {
            catVariantJson.addProperty(
                catVariantRegistry.getKey(variant)!!.path,
                catVariantRegistry.getId(variant)
            )
        }
        miscJson.add("cat_variant", catVariantJson)

        val frogVariantRegistry = server.registryAccess().lookupOrThrow(Registries.FROG_VARIANT)
        val frogVariantJson = JsonObject()
        for (variant in frogVariantRegistry) {
            frogVariantJson.addProperty(
                frogVariantRegistry.getKey(variant)!!.path,
                frogVariantRegistry.getId(variant)
            )
        }
        miscJson.add("frog_variant", frogVariantJson)


        val directionJson = JsonObject()
        for (dir in Direction.entries) {
            directionJson.addProperty(dir.serializedName, dir.get3DDataValue())
        }
        miscJson.add("direction", directionJson)

        val entityPoseJson = JsonObject()
        val poses: Array<Pose> = Pose.entries.toTypedArray()
        for (i in poses.indices) {
            entityPoseJson.addProperty(poses[i].serializedName, i)
        }
        miscJson.add("entity_pose", entityPoseJson)

        val particleTypesJson = JsonObject()
        for (type in BuiltInRegistries.PARTICLE_TYPE) {
            particleTypesJson.addProperty(
                BuiltInRegistries.PARTICLE_TYPE.getKey(type)!!.path,
                BuiltInRegistries.PARTICLE_TYPE.getId(type)
            )
        }
        miscJson.add("particle_type", particleTypesJson)

        val snifferStateJson = JsonObject()

        for (state in Sniffer.State.entries.toTypedArray()) {
            snifferStateJson.addProperty(state.name.lowercase(), state.ordinal)
        }
        miscJson.add("sniffer_state", snifferStateJson)

        val trackedDataHandlerJson = JsonObject()
        for (field in EntityDataSerializers::class.java.declaredFields) {
            field.isAccessible = true
            if (Modifier.isStatic(field.modifiers)) {
                val handler = field.get(null) as? EntityDataSerializer<*> ?: continue
                val name = field.name.lowercase()
                val id = EntityDataSerializers.getSerializedId(handler)

                trackedDataHandlerJson.addProperty(name, id)
            }
        }
        miscJson.add("tracked_data_handler", trackedDataHandlerJson)

        return miscJson
    }
}