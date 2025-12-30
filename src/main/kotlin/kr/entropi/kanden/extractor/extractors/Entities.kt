package kr.entropi.kanden.extractor.extractors

import com.google.gson.*
import com.mojang.authlib.GameProfile
import kr.entropi.kanden.extractor.ClassComparator
import kr.entropi.kanden.extractor.Extractor
import kr.entropi.kanden.extractor.dummy.DummyPlayerEntity
import kr.entropi.kanden.extractor.jsonNullable
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.Holder
import net.minecraft.core.Rotations
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import net.minecraft.world.entity.animal.armadillo.Armadillo
import net.minecraft.world.entity.animal.golem.CopperGolemState
import net.minecraft.world.entity.animal.sniffer.Sniffer
import net.minecraft.world.entity.npc.villager.VillagerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.level.block.state.BlockState
import org.joml.Quaternionfc
import org.joml.Vector3fc
import java.lang.reflect.ParameterizedType
import java.util.*

class Entities : Extractor.Extractor {
    lateinit var world: ServerLevel

    private fun <T> mapOptionalToJson(value: Optional<T>, f: (T) -> JsonElement): JsonElement {
        return value.map(f).orElse(JsonNull.INSTANCE)
    }

    private fun mapRegistryIdToJson(value: Any): JsonPrimitive {
        val value = value as Holder<*>
        return JsonPrimitive(value.registeredName)
    }

    @Suppress("UNCHECKED_CAST")
    private fun trackedDataToJson(data: EntityDataAccessor<*>, tracker: SynchedEntityData): Pair<String, JsonElement> {
        val handler: EntityDataSerializer<*> = data.serializer()
        val value: Any = tracker.get(data)

        return when (handler) {
            EntityDataSerializers.BYTE -> {
                "byte" to JsonPrimitive(value as Byte)
            }

            EntityDataSerializers.INT -> {
                "integer" to JsonPrimitive(value as Int)
            }

            EntityDataSerializers.LONG -> {
                "long" to JsonPrimitive(value as Long)
            }

            EntityDataSerializers.FLOAT -> {
                "float" to JsonPrimitive(value as Float)
            }

            EntityDataSerializers.STRING -> {
                "string" to JsonPrimitive(value as String)
            }

            EntityDataSerializers.COMPONENT -> {
                "text_component" to JsonPrimitive((value as Component).string)
            }

            EntityDataSerializers.OPTIONAL_COMPONENT -> {
                "optional_text_component" to mapOptionalToJson(value as Optional<Component>) {
                    JsonPrimitive(
                        it.string
                    )
                }
            }

            EntityDataSerializers.ITEM_STACK -> {
                "item_stack" to JsonPrimitive((value as ItemStack).toString())
            }

            EntityDataSerializers.BLOCK_STATE -> {
                "block_state" to JsonPrimitive((value as BlockState).toString())
            }

            EntityDataSerializers.OPTIONAL_BLOCK_STATE -> {
                "optional_block_state" to mapOptionalToJson(value as Optional<BlockState>) {
                    JsonPrimitive(
                        it.toString()
                    )
                }
            }

            EntityDataSerializers.BOOLEAN -> {
                "boolean" to JsonPrimitive(value as Boolean)
            }

            EntityDataSerializers.PARTICLE -> {
                val id = BuiltInRegistries.PARTICLE_TYPE.getKey((value as ParticleOptions).type)!!
                "particle" to JsonPrimitive(id.path)
            }

            EntityDataSerializers.PARTICLES -> {
                val particleList = value as List<ParticleOptions>
                val json = JsonArray()
                for (particle in particleList) {
                    val id = BuiltInRegistries.PARTICLE_TYPE.getKey(particle.type)!!
                    json.add(id.path)
                }

                "particle_list" to json
            }

            EntityDataSerializers.ROTATIONS -> {
                val rotation = value as Rotations
                val json = JsonObject()
                json.addProperty("pitch", rotation.x)
                json.addProperty("yaw", rotation.y)
                json.addProperty("roll", rotation.z)

                "rotation" to json
            }

            EntityDataSerializers.BLOCK_POS -> {
                val blockPos = value as BlockPos
                val json = JsonObject()
                json.addProperty("x", blockPos.x)
                json.addProperty("y", blockPos.y)
                json.addProperty("z", blockPos.z)

                "block_pos" to json
            }

            EntityDataSerializers.OPTIONAL_BLOCK_POS -> {
                "optional_block_pos" to mapOptionalToJson(value as Optional<BlockPos>) {
                    val json = JsonObject()
                    json.addProperty("x", it.x)
                    json.addProperty("y", it.y)
                    json.addProperty("z", it.z)

                    json
                }
            }

            EntityDataSerializers.DIRECTION -> {
                "facing" to JsonPrimitive(value.toString())
            }

            EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE -> {
                "optional_living_entity_reference" to mapOptionalToJson(value as Optional<EntityReference<LivingEntity>>) {
                    JsonPrimitive(
                        it.uuid.toString()
                    )
                }
            }

            EntityDataSerializers.OPTIONAL_GLOBAL_POS -> {
                "optional_global_pos" to mapOptionalToJson(value as Optional<GlobalPos>) {
                    val position = JsonObject()
                    position.addProperty("x", it.pos().x)
                    position.addProperty("y", it.pos().y)
                    position.addProperty("z", it.pos().z)

                    val json = JsonObject()
                    json.add("position", position)
                    json.addProperty("dimension", it.dimension.identifier().path)

                    json
                }
            }

            EntityDataSerializers.VILLAGER_DATA -> {
                val villagerData = value as VillagerData

                val json = JsonObject()
                val type = BuiltInRegistries.VILLAGER_TYPE.getKey(villagerData.type.value()).path
                val profession = BuiltInRegistries.VILLAGER_PROFESSION.getKey(villagerData.profession.value()).path
                json.addProperty("type", type)
                json.addProperty("profession", profession)
                json.addProperty("level", villagerData.level)

                "villager_data" to json
            }

            EntityDataSerializers.OPTIONAL_UNSIGNED_INT -> {
                val value = value as OptionalInt
                "optional_int" to if (value.isPresent) JsonPrimitive(value.asInt) else JsonNull.INSTANCE
            }

            EntityDataSerializers.POSE -> {
                "entity_pose" to JsonPrimitive((value as Pose).serializedName)
            }

            EntityDataSerializers.CAT_VARIANT -> {
                "cat_variant" to mapRegistryIdToJson(value)
            }

            EntityDataSerializers.CHICKEN_VARIANT -> {
                "chicken_variant" to mapRegistryIdToJson(value)
            }

            EntityDataSerializers.COW_VARIANT -> {
                "cow_variant" to mapRegistryIdToJson(value)
            }

            EntityDataSerializers.WOLF_VARIANT -> {
                "wolf_variant" to mapRegistryIdToJson(value)
            }

            EntityDataSerializers.WOLF_SOUND_VARIANT -> {
                "wolf_sound_variant" to mapRegistryIdToJson(value)
            }

            EntityDataSerializers.FROG_VARIANT -> {
                "frog_variant" to mapRegistryIdToJson(value)
            }

            EntityDataSerializers.PIG_VARIANT -> {
                "pig_variant" to mapRegistryIdToJson(value)
            }

            EntityDataSerializers.ZOMBIE_NAUTILUS_VARIANT -> {
                "zombie_nautilus_variant" to mapRegistryIdToJson(value)
            }

            EntityDataSerializers.PAINTING_VARIANT -> {
                "painting_variant" to mapRegistryIdToJson(value)
            }

            EntityDataSerializers.ARMADILLO_STATE -> {
                "armadillo_state" to JsonPrimitive((value as Armadillo.ArmadilloState).serializedName)
            }

            EntityDataSerializers.SNIFFER_STATE -> {
                "sniffer_state" to JsonPrimitive((value as Sniffer.State).name.lowercase())
            }

            EntityDataSerializers.WEATHERING_COPPER_STATE -> {
                "oxidation_level" to JsonPrimitive(
                    (value as WeatheringCopper.WeatherState).serializedName
                )
            }

            EntityDataSerializers.COPPER_GOLEM_STATE -> {
                "copper_golem_state" to JsonPrimitive((value as CopperGolemState).serializedName)
            }

            EntityDataSerializers.VECTOR3 -> {
                val vector = value as Vector3fc
                val json = JsonObject()
                json.addProperty("x", vector.x())
                json.addProperty("y", vector.y())
                json.addProperty("z", vector.z())

                "vector3f" to json
            }

            EntityDataSerializers.QUATERNION -> {
                val vector = value as Quaternionfc
                val json = JsonObject()
                json.addProperty("x", vector.x())
                json.addProperty("y", vector.y())
                json.addProperty("z", vector.z())
                json.addProperty("w", vector.w())

                "quaternionf" to json
            }


            EntityDataSerializers.RESOLVABLE_PROFILE -> {
                val profile = value as ResolvableProfile

                "profile" to mapOptionalToJson(profile.name()) { JsonPrimitive(it) }
            }

            EntityDataSerializers.HUMANOID_ARM -> {
                val arm = value as HumanoidArm

                "arm" to JsonPrimitive(arm.serializedName)
            }

            else -> {
                println("${handler.javaClass}")

                throw IllegalArgumentException(
                    "Unexpected tracked handler of ID ${EntityDataSerializers.getSerializedId(handler)}: $handler"
                )
            }
        }
    }

    override fun extract(server: MinecraftServer): JsonElement {
        this.world = server.overworld()

        val entityList = mutableListOf<Pair<Class<out Entity>, EntityType<*>?>>()
        val entityClassTypeMap = HashMap<Class<out Entity?>, EntityType<*>>()
        for (f in EntityType::class.java.fields) {
            if (f.type == EntityType::class.java) {
                @Suppress("UNCHECKED_CAST") val entityClass =
                    (f.genericType as ParameterizedType).actualTypeArguments[0] as Class<out Entity?>
                val entityType = f.get(null) as EntityType<*>?
                println(entityType?.javaClass?.simpleName)

                entityList.add(entityClass to entityType!!)
                entityClassTypeMap.put(entityClass, entityType)
            }
        }

        val dataTrackerField = Entity::class.java.getDeclaredField("dataTracker")
        dataTrackerField.isAccessible = true

        val entitiesMap = TreeMap<Class<out Entity>, JsonElement?>(ClassComparator())

        for (entry in entityList) {
            var (entityClass, entityType) = entry

            // While we can use the tracked data registry and reflection to get the tracked
            // fields on entities, we won't know what their default values are because they
            // are assigned in the entity's constructor.
            // To obtain this, we create a dummy world to spawn the entities into and read
            // the data tracker field from the base entity class.
            // We also handle player entities specially since they cannot be spawned with
            // EntityType#create.
            val entityInstance: Entity = if (entityType == EntityType.PLAYER) DummyPlayerEntity(
                world, GameProfile(UUID.randomUUID(), "dummy")
            )
            else entityType!!.create(world, EntitySpawnReason.COMMAND)!!


            val dataTracker = dataTrackerField.get(entityInstance) as SynchedEntityData

            while (entitiesMap.get(entityClass) == null) {
                val entityJson = JsonObject()

                val parent = entityClass.superclass
                val hasParent = null != parent && Entity::class.java.isAssignableFrom(parent)

                if (hasParent) {
                    entityJson.addProperty("parent", parent.simpleName)
                }

                if (entityType != null) {
                    entityJson.addProperty("type", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).path)
                    entityJson.add("translation_key", jsonNullable(entityType.descriptionId))
                }


                val fieldsJson = JsonArray()
                for (entityField in entityClass.declaredFields) {
                    if (entityField.type.equals(EntityDataAccessor::class.java)) {
                        entityField.isAccessible = true

                        println("${entityField.declaringClass.simpleName} -> ${entityField.name}")
                        val trackedData = entityField.get(null) as EntityDataAccessor<*>


                        val fieldJson = JsonObject()
                        val fieldName = entityField.name.lowercase()
                        fieldJson.addProperty("name", fieldName)
                        fieldJson.addProperty("index", trackedData.id())

                        val data = trackedDataToJson(trackedData, dataTracker)
                        fieldJson.addProperty("type", data.first)
                        fieldJson.add("default_value", data.second)

                        fieldsJson.add(fieldJson)
                    }
                }
                entityJson.add("fields", fieldsJson)

                if (entityInstance is LivingEntity) {
                    @Suppress("UNCHECKED_CAST") val type = entityType as EntityType<out LivingEntity>

                    val defaultAttributes = DefaultAttributes.getSupplier(type)
                    val attributesJson = JsonArray()
                    val instancesField = defaultAttributes.javaClass.getDeclaredField("instances")
                    instancesField.isAccessible = true
                    @Suppress("UNCHECKED_CAST") val instances =
                        instancesField.get(defaultAttributes) as MutableMap<Attribute?, AttributeInstance>

                    for (instance in instances.values) {
                        val attribute = instance.attribute.value()

                        val attributeJson = JsonObject()

                        attributeJson.addProperty("id", BuiltInRegistries.ATTRIBUTE.getId(attribute))
                        attributeJson.addProperty("name", BuiltInRegistries.ATTRIBUTE.getKey(attribute)!!.path)
                        attributeJson.addProperty("base_value", instance.baseValue)

                        attributesJson.add(attributeJson)
                    }
                    entityJson.add("attributes", attributesJson)
                }

                entityInstance.boundingBox.run {
                    val boundingBoxJson = JsonObject()

                    boundingBoxJson.addProperty("size_x", xsize)
                    boundingBoxJson.addProperty("size_y", ysize)
                    boundingBoxJson.addProperty("size_z", zsize)

                    entityJson.add("default_bounding_box", boundingBoxJson)
                }

                entitiesMap.put(entityClass, entityJson)

                if (!hasParent) {
                    break
                }

                entityClass = parent as Class<out Entity>
                entityType = entityClassTypeMap[entityClass]
            }
        }

        val entitiesJson = JsonObject()
        for (entry in entitiesMap.entries) {
            entitiesJson.add(entry.key.simpleName, entry.value)
        }

        return entitiesJson
    }

    override fun fileName(): String {
        return "entities.json"
    }
}
