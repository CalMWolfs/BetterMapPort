package com.calmwolfs.bettermap.data

import com.calmwolfs.bettermap.events.FooterUpdateEvent
import com.calmwolfs.bettermap.events.ModTickEvent
import com.calmwolfs.bettermap.events.TablistUpdateEvent
import com.calmwolfs.bettermap.mixins.transformers.AccessorGuiPlayerTabOverlay
import com.calmwolfs.bettermap.utils.StringUtils.stripResets
import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.world.WorldSettings
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object TablistData {
    private var tablist = listOf<String>()
    private var header = ""
    private var footer = ""
    private var playerInfo = listOf<NetworkPlayerInfo>()

    private var tablistObjective = ""

    private val playerOrdering = Ordering.from(PlayerComparator())

    fun getTablist() = tablist
    fun getPlayerInfo() = playerInfo
    fun getHeader() = header
    fun getFooter() = footer

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTick(event: ModTickEvent) {
        val thePlayer = Minecraft.getMinecraft()?.thePlayer ?: return
        if (thePlayer.sendQueue == null) return

        val players = playerOrdering.sortedCopy(thePlayer.sendQueue.playerInfoMap)
        val result = mutableListOf<String>()

        for (player in players) {
            val name = Minecraft.getMinecraft().ingameGUI.tabList.getPlayerName(player).stripResets()
            result.add(name)
        }

        val tablistData = Minecraft.getMinecraft().ingameGUI.tabList as AccessorGuiPlayerTabOverlay
        header = tablistData.bettermap_getHeader()?.formattedText ?: ""
        val footerData = tablistData.bettermap_getFooter()?.formattedText ?: ""

        if (footerData != footer && footerData != "") {
            FooterUpdateEvent(footerData).postAndCatch()
            footer = footerData
        }

        if (result == tablist) return

        tablist = result
        playerInfo = players
        TablistUpdateEvent(tablist).postAndCatch()
    }

    @SideOnly(Side.CLIENT)
    internal class PlayerComparator : Comparator<NetworkPlayerInfo> {
        override fun compare(o1: NetworkPlayerInfo, o2: NetworkPlayerInfo): Int {
            val team1 = o1.playerTeam
            val team2 = o2.playerTeam
            return ComparisonChain.start().compareTrueFirst(
                o1.gameType != WorldSettings.GameType.SPECTATOR,
                o2.gameType != WorldSettings.GameType.SPECTATOR
            ).compare(
                if (team1 != null) team1.registeredName else "",
                if (team2 != null) team2.registeredName else ""
            ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
        }
    }
}