package kr.entropi.kanden.extractor.extractors

import com.google.gson.*
import com.mojang.authlib.GameProfile
import kr.entropi.kanden.extractor.ClassComparator
import kr.entropi.kanden.extractor.Extractor
import kr.entropi.kanden.extractor.dummy.DummyPlayerEntity
import kr.entropi.kanden.extractor.jsonNullable
import net.minecraft.block.BlockState
import net.minecraft.block.Oxidizable
import net.minecraft.component.type.ProfileComponent
import net.minecraft.entity.*
import net.minecraft.entity.attribute.DefaultAttributeRegistry
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeInstance
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.decoration.MannequinEntity
import net.minecraft.entity.passive.*
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Arm
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.EulerAngle
import net.minecraft.util.math.GlobalPos
import net.minecraft.village.VillagerData
import org.joml.Quaternionfc
import org.joml.Vector3fc
import java.lang.reflect.ParameterizedType
import java.util.*

class Entities : Extractor.Extractor {
    lateinit var world: ServerWorld

    private fun <T> mapOptionalToJson(value: Optional<T>, f: (T) -> JsonElement): JsonElement {
        return value.map(f).orElse(JsonNull.INSTANCE)
    }

    private fun mapRegistryIdToJson(value: Any): JsonPrimitive {
        val value = value as RegistryEntry<*>
        return JsonPrimitive(value.idAsString)
    }

    @Suppress("UNCHECKED_CAST")
    private fun trackedDataToJson(data: TrackedData<*>, tracker: DataTracker): Pair<String, JsonElement> {
        val handler: TrackedDataHandler<*> = data.dataType()
        val value: Any = tracker.get(data)

        return when (handler) {
            TrackedDataHandlerRegistry.BYTE -> {
                "byte" to JsonPrimitive(value as Byte)
            }

            TrackedDataHandlerRegistry.INTEGER -> {
                "integer" to JsonPrimitive(value as Int)
            }

            TrackedDataHandlerRegistry.LONG -> {
                "long" to JsonPrimitive(value as Long)
            }

            TrackedDataHandlerRegistry.FLOAT -> {
                "float" to JsonPrimitive(value as Float)
            }

            TrackedDataHandlerRegistry.STRING -> {
                "string" to JsonPrimitive(value as String)
            }

            TrackedDataHandlerRegistry.TEXT_COMPONENT -> {
                "text_component" to JsonPrimitive((value as Text).string)
            }

            TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT -> {
                "optional_text_component" to mapOptionalToJson(value as Optional<Text>) {
                    JsonPrimitive(
                        it.string
                    )
                }
            }

            TrackedDataHandlerRegistry.ITEM_STACK -> {
                "item_stack" to JsonPrimitive((value as ItemStack).toString())
            }

            TrackedDataHandlerRegistry.BLOCK_STATE -> {
                "block_state" to JsonPrimitive((value as BlockState).toString())
            }

            TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE -> {
                "optional_block_state" to mapOptionalToJson(value as Optional<BlockState>) {
                    JsonPrimitive(
                        it.toString()
                    )
                }
            }

            TrackedDataHandlerRegistry.BOOLEAN -> {
                "boolean" to JsonPrimitive(value as Boolean)
            }

            TrackedDataHandlerRegistry.PARTICLE -> {
                val id = Registries.PARTICLE_TYPE.getId((value as ParticleEffect).type)!!
                "particle" to JsonPrimitive(id.path)
            }

            TrackedDataHandlerRegistry.PARTICLE_LIST -> {
                val particleList = value as List<ParticleEffect>
                val json = JsonArray()
                for (particle in particleList) {
                    val id = Registries.PARTICLE_TYPE.getId(particle.type)!!
                    json.add(id.path)
                }

                "particle_list" to json
            }

            TrackedDataHandlerRegistry.ROTATION -> {
                val rotation = value as EulerAngle
                val json = JsonObject()
                json.addProperty("pitch", rotation.pitch)
                json.addProperty("yaw", rotation.yaw)
                json.addProperty("roll", rotation.roll)

                "rotation" to json
            }

            TrackedDataHandlerRegistry.BLOCK_POS -> {
                val blockPos = value as BlockPos
                val json = JsonObject()
                json.addProperty("x", blockPos.x)
                json.addProperty("y", blockPos.y)
                json.addProperty("z", blockPos.z)

                "block_pos" to json
            }

            TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS -> {
                "optional_block_pos" to mapOptionalToJson(value as Optional<BlockPos>) {
                    val json = JsonObject()
                    json.addProperty("x", it.x)
                    json.addProperty("y", it.y)
                    json.addProperty("z", it.z)

                    json
                }
            }

            TrackedDataHandlerRegistry.FACING -> {
                "facing" to JsonPrimitive(value.toString())
            }

            TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE -> {
                "lazy_entity_reference" to mapOptionalToJson(value as Optional<LazyEntityReference<LivingEntity>>) {
                    JsonPrimitive(
                        it.uuid.toString()
                    )
                }
            }

            TrackedDataHandlerRegistry.OPTIONAL_GLOBAL_POS -> {
                "optional_global_pos" to mapOptionalToJson(value as Optional<GlobalPos>) {
                    val position = JsonObject()
                    position.addProperty("x", it.pos().x)
                    position.addProperty("y", it.pos().y)
                    position.addProperty("z", it.pos().z)

                    val json = JsonObject()
                    json.add("position", position)
                    json.addProperty("dimension", it.dimension().value.toString())

                    json
                }
            }

            TrackedDataHandlerRegistry.VILLAGER_DATA -> {
                val villagerData = value as VillagerData
                villagerData.type

                val json = JsonObject()
                val type = Registries.VILLAGER_TYPE.getId(villagerData.type.value()).path
                val profession = Registries.VILLAGER_PROFESSION.getId(villagerData.profession.value()).path
                json.addProperty("type", type)
                json.addProperty("profession", profession)
                json.addProperty("level", villagerData.level)

                "villager_data" to json
            }

            TrackedDataHandlerRegistry.OPTIONAL_INT -> {
                val value = value as OptionalInt
                "optional_int" to if (value.isPresent) JsonPrimitive(value.asInt) else JsonNull.INSTANCE
            }

            TrackedDataHandlerRegistry.ENTITY_POSE -> {
                "entity_pose" to JsonPrimitive((value as EntityPose).asString())
            }

            TrackedDataHandlerRegistry.CAT_VARIANT -> {
                "cat_variant" to mapRegistryIdToJson(value)
            }

            TrackedDataHandlerRegistry.CHICKEN_VARIANT -> {
                "chicken_variant" to mapRegistryIdToJson(value)
            }

            TrackedDataHandlerRegistry.COW_VARIANT -> {
                "cow_variant" to mapRegistryIdToJson(value)
            }

            TrackedDataHandlerRegistry.WOLF_VARIANT -> {
                "wolf_variant" to mapRegistryIdToJson(value)
            }

            TrackedDataHandlerRegistry.WOLF_SOUND_VARIANT -> {
                "wolf_sound_variant" to mapRegistryIdToJson(value)
            }

            TrackedDataHandlerRegistry.FROG_VARIANT -> {
                "frog_variant" to mapRegistryIdToJson(value)
            }

            TrackedDataHandlerRegistry.PIG_VARIANT -> {
                "pig_variant" to mapRegistryIdToJson(value)
            }

            TrackedDataHandlerRegistry.ZOMBIE_NAUTILUS_VARIANT -> {
                "zombie_nautilus_variant" to mapRegistryIdToJson(value)
            }

            TrackedDataHandlerRegistry.PAINTING_VARIANT -> {
                "painting_variant" to mapRegistryIdToJson(value)
            }

            TrackedDataHandlerRegistry.ARMADILLO_STATE -> {
                "armadillo_state" to JsonPrimitive((value as ArmadilloEntity.State).asString())
            }

            TrackedDataHandlerRegistry.SNIFFER_STATE -> {
                "sniffer_state" to JsonPrimitive((value as SnifferEntity.State).name.lowercase())
            }

            TrackedDataHandlerRegistry.OXIDATION_LEVEL -> {
                "oxidation_level" to JsonPrimitive(
                    (value as Oxidizable.OxidationLevel).asString()
                )
            }

            TrackedDataHandlerRegistry.COPPER_GOLEM_STATE -> {
                "copper_golem_state" to JsonPrimitive((value as CopperGolemState).asString())
            }

            TrackedDataHandlerRegistry.VECTOR_3F -> {
                val vector = value as Vector3fc
                val json = JsonObject()
                json.addProperty("x", vector.x())
                json.addProperty("y", vector.y())
                json.addProperty("z", vector.z())

                "vector3f" to json
            }

            TrackedDataHandlerRegistry.QUATERNION_F -> {
                val vector = value as Quaternionfc
                val json = JsonObject()
                json.addProperty("x", vector.x())
                json.addProperty("y", vector.y())
                json.addProperty("z", vector.z())
                json.addProperty("w", vector.w())

                "quaternionf" to json
            }


            TrackedDataHandlerRegistry.PROFILE -> {
                val profile = value as ProfileComponent

                "profile" to mapOptionalToJson(profile.name) { JsonPrimitive(it) }
            }

            TrackedDataHandlerRegistry.ARM -> {
                val arm = value as Arm

                "arm" to JsonPrimitive(arm.asString())
            }

            else -> {
                println("${handler.javaClass}")

                throw IllegalArgumentException(
                    "Unexpected tracked handler of ID ${TrackedDataHandlerRegistry.getId(handler)}$handler"
                )
            }
        }
    }

    override fun extract(server: MinecraftServer): JsonElement {
        this.world = server.overworld

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
            else entityType!!.create(world, null)!!


            val dataTracker = dataTrackerField.get(entityInstance) as DataTracker

            while (entitiesMap.get(entityClass) == null) {
                val entityJson = JsonObject()

                val parent = entityClass.superclass
                val hasParent = null != parent && Entity::class.java.isAssignableFrom(parent)

                if (hasParent) {
                    entityJson.addProperty("parent", parent.simpleName)
                }

                if (entityType != null) {
                    entityJson.addProperty("type", Registries.ENTITY_TYPE.getId(entityType).path)
                    entityJson.add("translation_key", jsonNullable(entityType.translationKey))
                }


                val fieldsJson = JsonArray()
                for (entityField in entityClass.declaredFields) {
                    if (entityField.type.equals(TrackedData::class.java)) {
                        entityField.isAccessible = true

                        println("${entityField.declaringClass.simpleName} -> ${entityField.name}")
                        val trackedData = entityField.get(null) as TrackedData<*>


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
                    @Suppress("UNCHECKED_CAST") val type = entityType as? EntityType<out LivingEntity>
                    val defaultAttributes = DefaultAttributeRegistry.get(type)
                    val attributesJson = JsonArray()
                    if (null != defaultAttributes) {
                        val instancesField = defaultAttributes.javaClass.getDeclaredField("instances")
                        instancesField.isAccessible = true
                        @Suppress("UNCHECKED_CAST") val instances =
                            instancesField.get(defaultAttributes) as MutableMap<EntityAttribute?, EntityAttributeInstance>

                        for (instance in instances.values) {
                            val attribute = instance.attribute.value()

                            val attributeJson = JsonObject()

                            attributeJson.addProperty("id", Registries.ATTRIBUTE.getRawId(attribute))
                            attributeJson.addProperty("name", Registries.ATTRIBUTE.getId(attribute)!!.getPath())
                            attributeJson.addProperty("base_value", instance.baseValue)

                            attributesJson.add(attributeJson)
                        }
                    }
                    entityJson.add("attributes", attributesJson)
                }

                entityInstance.boundingBox?.run {
                    val boundingBoxJson = JsonObject()

                    boundingBoxJson.addProperty("size_x", lengthX)
                    boundingBoxJson.addProperty("size_y", lengthY)
                    boundingBoxJson.addProperty("size_z", lengthZ)

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
