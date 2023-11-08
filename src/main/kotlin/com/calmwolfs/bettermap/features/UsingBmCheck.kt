package com.calmwolfs.bettermap.features

import com.calmwolfs.bettermap.data.connection.BetterMapServer
import com.calmwolfs.bettermap.data.connection.SoopyConnection
import com.calmwolfs.bettermap.data.connection.SoopyPacket
import com.calmwolfs.bettermap.data.connection.SoopyPacketServer
import com.calmwolfs.bettermap.data.connection.SoopyPacketType
import com.calmwolfs.bettermap.events.HypixelJoinEvent
import com.calmwolfs.bettermap.utils.ChatUtils
import com.google.gson.JsonObject
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object UsingBmCheck {
    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        BetterMapServer.start()
    }

    fun command(args: Array<String>) {
        if (args.isEmpty()) {
            ChatUtils.chat("§e[BetterMap] Missing player name! use /bping <player>")
        }
        else {
            val usernames = args.toList()

            BetterMapServer.isUsingBMap(usernames) { usingBetterMap ->
                usernames.forEachIndexed { index, name ->
                    if (usingBetterMap[index]) {
                        ChatUtils.chat("§6[BetterMap] §3$name §7is using bettermap")
                    } else {
                        ChatUtils.chat("§6[BetterMap] §3$name §7is not using bettermap (or is offline)")
                    }
                }
            }
        }
    }

    fun debugPacket() {
        SoopyConnection.sendData(SoopyPacket(SoopyPacketType.DEBUG, SoopyPacketServer.API, JsonObject()))
    }
}