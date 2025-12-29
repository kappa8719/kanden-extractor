package kr.entropi.kanden.extractor.dummy

import com.mojang.authlib.GameProfile
import kr.entropi.kanden.extractor.Extractor
import kr.entropi.kanden.extractor.initiateWithoutConstructor
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.GameMode
import net.minecraft.world.World

class DummyPlayerEntity(world: World, profile: GameProfile) : PlayerEntity(world, profile) {
    companion object {
        val INSTANCE: DummyPlayerEntity = initiateWithoutConstructor(DummyPlayerEntity::class.java)

        init {
            INSTANCE.initDataTracker(DataTracker.Builder(INSTANCE))
        }
    }

    override fun getGameMode(): GameMode? {
        return GameMode.DEFAULT
    }
}