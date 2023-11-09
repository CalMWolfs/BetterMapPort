package com.calmwolfs.bettermap.utils

import com.calmwolfs.bettermap.events.DungeonStartEvent
import com.calmwolfs.bettermap.events.EnterBossFightEvent
import com.calmwolfs.bettermap.events.ModChatEvent
import com.calmwolfs.bettermap.events.WorldChangeEvent
import com.calmwolfs.bettermap.utils.StringUtils.matchMatcher
import com.calmwolfs.bettermap.utils.StringUtils.unformat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DungeonUtils {
    private val floorPattern = " §7⏣? §cThe Catacombs §7\\((?<floor>\\w+)\\)".toPattern()

    var dungeonFloor: String? = null
    var started = false
    var inBossRoom = false

    fun inDungeon() = dungeonFloor != null

    @SubscribeEvent
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (dungeonFloor == null) {
            for (line in event.scoreboard) {
                floorPattern.matchMatcher(line) {
                    val floor = group("floor")
                    dungeonFloor = floor
                }
            }
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ModChatEvent) {
        val floor = dungeonFloor ?: return
        if (event.message == "§e[NPC] §bMort§f: Here, I found this map when I first entered the dungeon.") {
            started = true
            DungeonStartEvent(floor).postAndCatch()
        }

        if (!inBossRoom) {
            if (event.message.unformat() in bossEntryMessages) {
                inBossRoom = true
                EnterBossFightEvent(floor).postAndCatch()
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        dungeonFloor = null
        started = false
        inBossRoom = false
    }

    private val bossEntryMessages = listOf(
        "[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable.",
        "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
        "[BOSS] The Professor: I was burdened with terrible news recently...",
        "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
        "[BOSS] Livid: Welcome, you arrive right on time. I am Livid, the Master of Shadows.",
        "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!",
        "[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!"
    )
}