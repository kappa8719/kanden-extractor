package kr.entropi.kanden.extractor.dummy

import com.mojang.authlib.GameProfile
import kr.entropi.kanden.extractor.initiateWithoutConstructor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType

class DummyPlayerEntity(world: ServerLevel, profile: GameProfile) : Player(world, profile) {
    companion object {
        val INSTANCE: DummyPlayerEntity = initiateWithoutConstructor(DummyPlayerEntity::class.java)

        init {
            INSTANCE.defineSynchedData(SynchedEntityData.Builder(INSTANCE))
        }
    }

    override fun gameMode(): GameType? {
        return GameType.DEFAULT_MODE
    }
}