package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.StandingAndWallBlockItem
import net.minecraft.world.level.EmptyBlockGetter
import java.util.*
import java.util.function.Supplier

class Blocks : Extractor.Extractor {
    override fun fileName(): String {
        return "blocks.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val topLevelJson = JsonObject()

        val blocksJson = JsonArray()
        var stateIdCounter = 0

        val shapes = LinkedHashMap<Shape, Int?>()

        for (block in BuiltInRegistries.BLOCK) {
            val key = BuiltInRegistries.BLOCK.getKey(block)
            val id = BuiltInRegistries.BLOCK.getId(block)

            val blockJson = JsonObject()
            blockJson.addProperty("id", id)
            blockJson.addProperty("name", key.path)
            blockJson.addProperty("translation_key", block.descriptionId)
            blockJson.addProperty("item_id", BuiltInRegistries.ITEM.getId(block.asItem()))

            val item = block.asItem()
            if (item is StandingAndWallBlockItem) {
                if (item.block === block) {
                    val wallBlock = item.wallBlock
                    blockJson.addProperty("wall_variant_id", BuiltInRegistries.BLOCK.getId(wallBlock))
                }
            }

            val propsJson = JsonArray()
            for (prop in block.stateDefinition.properties) {
                val propJson = JsonObject()

                propJson.addProperty("name", prop.name)

                val valuesJson = JsonArray()
                for (value in prop.allValues) {
                    valuesJson.add(value.toString().lowercase())
                }
                propJson.add("values", valuesJson)

                propsJson.add(propJson)
            }
            blockJson.add("properties", propsJson)

            val statesJson = JsonArray()
            for (state in block.stateDefinition.possibleStates) {
                val stateJson = JsonObject()
                val id = stateIdCounter
                stateIdCounter++
                stateJson.addProperty("id", id)
                stateJson.addProperty("luminance", state.lightEmission)
                stateJson.addProperty("opaque", state.canOcclude())
                stateJson.addProperty("replaceable", state.canBeReplaced())

                if (block.defaultBlockState() == state) {
                    blockJson.addProperty("default_state_id", id)
                }

                val collisionShapeIdxsJson = JsonArray()
                for (box in state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).toAabbs()) {
                    val collisionShape = Shape(
                        box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ
                    )

                    val idx = shapes.putIfAbsent(collisionShape, shapes.size)
                    collisionShapeIdxsJson.add(Objects.requireNonNullElseGet(idx, Supplier { shapes.size - 1 }))
                }

                stateJson.add("collision_shapes", collisionShapeIdxsJson)

                for (blockEntity in BuiltInRegistries.BLOCK_ENTITY_TYPE) {
                    if (blockEntity.isValid(state)) {
                        stateJson.addProperty("block_entity_type", BuiltInRegistries.BLOCK_ENTITY_TYPE.getId(blockEntity))
                    }
                }

                statesJson.add(stateJson)
            }
            blockJson.add("states", statesJson)

            blocksJson.add(blockJson)
        }

        val blockEntitiesJson = JsonArray()
        for (blockEntity in BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            val id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getId(blockEntity)
            val key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity)!!

            val blockEntityJson = JsonObject()
            blockEntityJson.addProperty("id", id)
            blockEntityJson.addProperty("ident", key.toString())
            blockEntityJson.addProperty("name", key.path)

            blockEntitiesJson.add(blockEntityJson)
        }

        val shapesJson = JsonArray()
        for (shape in shapes.keys) {
            val shapeJson = JsonObject()
            shapeJson.addProperty("min_x", shape.minX)
            shapeJson.addProperty("min_y", shape.minY)
            shapeJson.addProperty("min_z", shape.minZ)
            shapeJson.addProperty("max_x", shape.maxX)
            shapeJson.addProperty("max_y", shape.maxY)
            shapeJson.addProperty("max_z", shape.maxZ)
            shapesJson.add(shapeJson)
        }

        topLevelJson.add("block_entity_types", blockEntitiesJson)
        topLevelJson.add("shapes", shapesJson)
        topLevelJson.add("blocks", blocksJson)

        return topLevelJson
    }

    data class Shape(
        val minX: Double, val minY: Double, val minZ: Double, val maxX: Double, val maxY: Double, val maxZ: Double
    )
}