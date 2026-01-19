package kr.entropi.kanden.extractor.extractors

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.entropi.kanden.extractor.Extractor
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.protocol.configuration.ConfigurationProtocols
import net.minecraft.network.protocol.game.GameProtocols
import net.minecraft.network.protocol.handshake.HandshakeProtocols
import net.minecraft.network.protocol.login.LoginProtocols
import net.minecraft.network.protocol.status.StatusProtocols
import net.minecraft.server.MinecraftServer

class Packets : Extractor.Extractor {
    override fun fileName(): String {
        return "packets.json"
    }

    override fun extract(server: MinecraftServer): JsonElement {
        val packetsJson = JsonArray()

        HandshakeProtocols.SERVERBOUND_TEMPLATE
        serializeFactory(HandshakeProtocols.SERVERBOUND_TEMPLATE, packetsJson)
        serializeFactory(StatusProtocols.SERVERBOUND_TEMPLATE, packetsJson)
        serializeFactory(StatusProtocols.CLIENTBOUND_TEMPLATE, packetsJson)
        serializeFactory(LoginProtocols.SERVERBOUND_TEMPLATE, packetsJson)
        serializeFactory(LoginProtocols.CLIENTBOUND_TEMPLATE, packetsJson)
        serializeFactory(ConfigurationProtocols.SERVERBOUND_TEMPLATE, packetsJson)
        serializeFactory(ConfigurationProtocols.CLIENTBOUND_TEMPLATE, packetsJson)
        serializeFactory(GameProtocols.SERVERBOUND_TEMPLATE, packetsJson)
        serializeFactory(GameProtocols.CLIENTBOUND_TEMPLATE, packetsJson)

        return packetsJson
    }

    private fun serializeFactory(
        provider: ProtocolInfo.DetailsProvider,
        json: JsonArray
    ) {
        val details = provider.details()
        details.listPackets { type, i ->
            val packetJson = JsonObject()
            packetJson.addProperty("name", type.id().path)
            packetJson.addProperty("phase", details.id().id())
            packetJson.addProperty("side", details.flow().id())
            packetJson.addProperty("id", i)
            json.add(packetJson)
        }
    }
}