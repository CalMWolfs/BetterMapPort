package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.TablistData
import com.calmwolfs.bettermap.data.asGridPos
import com.calmwolfs.bettermap.events.EnterBossFightEvent
import com.calmwolfs.bettermap.events.ModChatEvent
import com.calmwolfs.bettermap.events.ModTickEvent
import com.calmwolfs.bettermap.events.WorldChangeEvent
import com.calmwolfs.bettermap.utils.DungeonUtils
import com.calmwolfs.bettermap.utils.MapUtils
import com.calmwolfs.bettermap.utils.StringUtils.matchMatcher
import com.calmwolfs.bettermap.utils.StringUtils.unformat
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Matcher

object MapTeam {
    private val deadPlayers = mutableListOf<String>()
    private val mapPlayers = mutableMapOf<Int, DungeonPlayer>()

    private var playerNick: String? = null

    private val playerDeathPattern = "\\s☠ (?<info>.*) and became a ghost\\.".toPattern()
    private val playerRevivePattern = "\\s❣ (?<info>.*) was revived.*!".toPattern()
    private val playerTabPattern = "^\\[\\d+] (?:\\[\\w+] )?(?<name>\\w+) .*\\((?<class>\\w+)(?: \\w+)*\\)\$".toPattern()

    fun getMapPlayers() = mapPlayers
    fun getDeadPlayers() = deadPlayers

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (!event.repeatSeconds(1)) return
        if (!DungeonUtils.inDungeon()) return
        updatePlayers()
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        deadPlayers.clear()
    }

    @SubscribeEvent
    fun onEnterBoss(event: EnterBossFightEvent) {
        deadPlayers.clear()
        mapPlayers.clear()
    }

    @SubscribeEvent
    fun onChatMessage(event: ModChatEvent) {
        if (!DungeonUtils.inDungeon()) return
        val unformatted = event.message.unformat()

        playerDeathPattern.matchMatcher(unformatted) {
            var player = group("info").split(" ")[0]

            if (player == "you") {
                player = Minecraft.getMinecraft().thePlayer.name
            }

            deadPlayers.add(player.lowercase())
            return
        }

        playerRevivePattern.matchMatcher(unformatted) {
            var player = group("info").split(" ")[0]

            if (player == "you") {
                player = Minecraft.getMinecraft().thePlayer.name
            }

            deadPlayers.remove(player.lowercase())
            return
        }
    }

    private fun updatePlayers() {
        var currentPlayer: Pair<NetworkPlayerInfo, Matcher>? = null
        var index = 0

        for (player in TablistData.getPlayerInfo()) {
            if (player.displayName == null) continue

            val tabLine = player.displayName.unformattedText.unformat()

            playerTabPattern.matchMatcher(tabLine) {
                val playerName = group("name")
                val playerClass = group("class")

                if (playerName == Minecraft.getMinecraft().thePlayer.name || playerName == playerNick) {
                    currentPlayer = Pair(player, this)
                } else {
                    if (!mapPlayers.containsKey(index)) mapPlayers[index] = DungeonPlayer(player, playerName)
                    val mapPlayer = mapPlayers[index] ?: return
                    mapPlayer.playerInfo = player
                    mapPlayer.updateTabInfo(playerName, playerClass)

                    index++
                }
            }
        }

        if (currentPlayer != null) {
            val (playerInfo, match) = currentPlayer ?: return
            val playerName = match.group("name")
            val playerClass = match.group("class")

            if (!mapPlayers.containsKey(index)) mapPlayers[index] = DungeonPlayer(playerInfo, playerName)
            val mapPlayer = mapPlayers[index] ?: return

            mapPlayer.playerInfo = playerInfo
            mapPlayer.updateTabInfo(playerName, playerClass)
        } else if (playerNick == null) {
            TablistData.getPlayerInfo().forEach { player ->
                if (player.gameProfile.id == Minecraft.getMinecraft().thePlayer.uniqueID) {
                    playerNick = player.gameProfile.name
                }
            }
        }

        for (player in mapPlayers.values) {
            val currentRoom = MapUtils.getRoom(player.position.asGridPos())
            if (player.currentRoom == currentRoom) continue
            //todo room exit event if saved room isnt null
            player.currentRoom = currentRoom
            //todo enter event if room isnt null
        }
    }
}
