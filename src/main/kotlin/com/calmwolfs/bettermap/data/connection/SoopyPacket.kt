package com.calmwolfs.bettermap.data.connection

import com.calmwolfs.bettermap.utils.JsonUtils.getIntOrValue
import com.google.gson.JsonObject

data class SoopyPacket(val type: SoopyPacketType, val server: SoopyPacketServer, val data: JsonObject)

fun JsonObject.toSoopyPacket(): SoopyPacket {
    val type = this.getIntOrValue("type")
    val server = this.getIntOrValue("server")
    val data = this.getAsJsonObject("data") ?: JsonObject()

    val packetType = SoopyPacketType.entries.getOrNull(type) ?: SoopyPacketType.DEBUG
    val packetServer = SoopyPacketServer.entries.getOrNull(server) ?: SoopyPacketServer.API

    return SoopyPacket(packetType, packetServer, data)
}

fun SoopyPacket.toJsonObject(): JsonObject {
    val output = JsonObject()
    output.addProperty("type", this.type.ordinal.toString())
    output.addProperty("server", this.server.ordinal.toString())
    output.add("data", this.data)

    return output
}

enum class SoopyPacketType {
    SUCCESS, DATA, JOIN_SERVER, PING, SERVER_REBOOT, DEBUG
}

enum class SoopyPacketServer(name: String, displayName: String, module: String) {
    API("soopyapis", "SoopyApi", "soopyApis"),
    TEST_CHAT("soopytestchatthing", "SoopyTestChatThing", "SoopyTestChatThing"),
    MINE_WAYPOINT("minewaypoints", "Mine Way Points", "minewaypoints"),
    SOOPYV2("soopyv2", "SoopyV2", "SoopyV2"),
    SBGBOT("sbgbot", "SbgBot", "sbgbot"),
    SOCKET_UTILS("socketutils", "SocketUtils", "socketUtils"),
    LEGALMAP("legalmap", "LegalMap", "LegalMap"),
    BETTERMAP("bettermap", "BetterMap", "BetterMap")
}
