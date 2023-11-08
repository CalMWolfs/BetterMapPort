package com.calmwolfs.bettermap.config

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.events.ConfigLoadEvent
import com.calmwolfs.bettermap.events.HypixelJoinEvent
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PlayerData {
    var playerSpecific: Storage.PlayerSpecific? = null

    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        val playerUuid = Minecraft.getMinecraft().thePlayer.uniqueID
        playerSpecific = BetterMapMod.feature.storage.players.getOrPut(playerUuid) { Storage.PlayerSpecific() }
        ConfigLoadEvent().postAndCatch()
    }
}