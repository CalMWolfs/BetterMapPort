package com.calmwolfs.bettermap.utils

import com.calmwolfs.bettermap.data.roomdata.RoomData
import com.calmwolfs.bettermap.data.roomdata.RoomDataManager
import com.calmwolfs.bettermap.events.DungeonStartEvent
import com.calmwolfs.bettermap.events.EnterBossFightEvent
import com.calmwolfs.bettermap.events.ModChatEvent
import com.calmwolfs.bettermap.events.WorldChangeEvent
import com.calmwolfs.bettermap.utils.StringUtils.matchMatcher
import com.calmwolfs.bettermap.utils.StringUtils.unformat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DungeonUtils {
    private val floorPattern = " §7⏣? §cThe Catacombs §7\\((?<floor>\\w+)\\)".toPattern()
    private val roomIdPattern = "\\d+/\\d+/\\d+ \\w+ (?<roomId>\\S+)".toPattern()

    private var dungeonFloor: String? = null
    private var started = false
    private var boss = false

    private var currentRoomId: String? = null
    var currentRoomData: RoomData? = null

    //todo in mastermode variable

    fun inDungeon() = started
    fun inDungeonRun() = started && !boss
    fun inBossRoom() = boss
    fun getDungeonFloor() = dungeonFloor

    @SubscribeEvent
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (getDungeonFloor() == null) {
            for (line in event.scoreboard) {
                if (getDungeonFloor() != null) continue
                floorPattern.matchMatcher(line) {
                    val floor = group("floor")
                    dungeonFloor = floor
                }
            }
        }

        var foundId: String? = null
        if (inDungeon() && !inBossRoom()) {
            if (event.scoreboard.isNotEmpty()) {
                roomIdPattern.matchMatcher(event.scoreboard[0].unformat()) {
                    foundId = group("roomId")
                }
            }
        }
        if (foundId != currentRoomId) {
            currentRoomData = RoomDataManager.getRoomData(foundId)
            currentRoomId = foundId
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ModChatEvent) {
        val floor = getDungeonFloor() ?: return
        if (event.message == "§e[NPC] §bMort§f: Here, I found this map when I first entered the dungeon.") {
            started = true
            DungeonStartEvent(floor).postAndCatch()
        }

        if (!inBossRoom()) {
            if (event.message.unformat() in bossEntryMessages) {
                boss = true
                EnterBossFightEvent(floor).postAndCatch()
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        dungeonFloor = null
        started = false
        boss = false
        currentRoomId = null
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