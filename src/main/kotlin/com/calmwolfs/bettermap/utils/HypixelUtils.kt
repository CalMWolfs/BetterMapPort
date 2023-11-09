package com.calmwolfs.bettermap.utils

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.events.HypixelJoinEvent
import com.calmwolfs.bettermap.utils.StringUtils.unformat
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object HypixelUtils {
    private var hypixelMain = false
    private var hypixelAlpha = false
    private var skyBlock = false

    val onHypixel get() = (hypixelMain || hypixelAlpha) && Minecraft.getMinecraft().thePlayer != null
    val onSkyBlock get() = (hypixelMain || hypixelAlpha) && Minecraft.getMinecraft().thePlayer != null

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        hypixelMain = false
        hypixelAlpha = false
        skyBlock = false
    }

    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        BetterMapMod.repo.displayRepoStatus(true)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (event.scoreboard.isEmpty()) return

        if (!onHypixel) {

            val last = event.scoreboard.last()
            hypixelMain = last == "§ewww.hypixel.net"
            hypixelAlpha = last == "§ealpha.hypixel.net"

            if (onHypixel) {
                HypixelJoinEvent().postAndCatch()
            }
        }
        if (!onHypixel) return

        skyBlock = event.objective.unformat().contains("SKYBLOCK")
    }
}
