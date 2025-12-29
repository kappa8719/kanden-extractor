package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.item.VerticallyAttachableBlockItem
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.EmptyBlockView
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

        for (block in Registries.BLOCK) {
            val blockJson = JsonObject()
            blockJson.addProperty("id", Registries.BLOCK.getRawId(block))
            blockJson.addProperty("name", Registries.BLOCK.getId(block).getPath())
            blockJson.addProperty("translation_key", block.getTranslationKey())
            blockJson.addProperty("item_id", Registries.ITEM.getRawId(block.asItem()))

            val item = block.asItem()
            if (item is VerticallyAttachableBlockItem) {
                if (item.block === block) {
                    val wallBlock = item.wallBlock
                    blockJson.addProperty("wall_variant_id", Registries.BLOCK.getRawId(wallBlock))
                }
            }

            val propsJson = JsonArray()
            for (prop in block.getStateManager().getProperties()) {
                val propJson = JsonObject()

                propJson.addProperty("name", prop.getName())

                val valuesJson = JsonArray()
                for (value in prop.getValues()) {
                    valuesJson.add(value.toString().lowercase())
                }
                propJson.add("values", valuesJson)

                propsJson.add(propJson)
            }
            blockJson.add("properties", propsJson)

            val statesJson = JsonArray()
            for (state in block.getStateManager().getStates()) {
                val stateJson = JsonObject()
                val id = stateIdCounter
                stateIdCounter++
                stateJson.addProperty("id", id)
                stateJson.addProperty("luminance", state.getLuminance())
                stateJson.addProperty("opaque", state.isOpaque())
                stateJson.addProperty("replaceable", state.isReplaceable())

                if (block.defaultState == state) {
                    blockJson.addProperty("default_state_id", id)
                }

                val collisionShapeIdxsJson = JsonArray()
                for (box in state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN).boundingBoxes) {
                    val collisionShape = Shape(
                        box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ
                    )

                    val idx = shapes.putIfAbsent(collisionShape, shapes.size)
                    collisionShapeIdxsJson.add(Objects.requireNonNullElseGet<Int?>(idx, Supplier { shapes.size - 1 }))
                }

                stateJson.add("collision_shapes", collisionShapeIdxsJson)

                for (blockEntity in Registries.BLOCK_ENTITY_TYPE) {
                    if (blockEntity.supports(state)) {
                        stateJson.addProperty("block_entity_type", Registries.BLOCK_ENTITY_TYPE.getRawId(blockEntity))
                    }
                }

                statesJson.add(stateJson)
            }
            blockJson.add("states", statesJson)

            blocksJson.add(blockJson)
        }

        val blockEntitiesJson = JsonArray()
        for (blockEntity in Registries.BLOCK_ENTITY_TYPE) {
            val blockEntityJson = JsonObject()
            blockEntityJson.addProperty("id", Registries.BLOCK_ENTITY_TYPE.getRawId(blockEntity))
            blockEntityJson.addProperty("ident", Registries.BLOCK_ENTITY_TYPE.getId(blockEntity).toString())
            blockEntityJson.addProperty("name", Registries.BLOCK_ENTITY_TYPE.getId(blockEntity)!!.getPath())

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