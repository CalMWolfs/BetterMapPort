package com.calmwolfs.bettermap.utils

import com.calmwolfs.bettermap.data.connection.BetterMapServer
import com.calmwolfs.bettermap.data.mapdata.DungeonMap
import com.calmwolfs.bettermap.data.mapdata.RoomState
import com.calmwolfs.bettermap.data.roomdata.RoomData
import com.calmwolfs.bettermap.data.roomdata.RoomDataManager
import com.calmwolfs.bettermap.events.DungeonStartEvent
import com.calmwolfs.bettermap.events.EnterBossFightEvent
import com.calmwolfs.bettermap.events.EntityDeathEvent
import com.calmwolfs.bettermap.events.ModChatEvent
import com.calmwolfs.bettermap.events.RoomChangeEvent
import com.calmwolfs.bettermap.events.WorldChangeEvent
import com.calmwolfs.bettermap.utils.StringUtils.findMatcher
import com.calmwolfs.bettermap.utils.StringUtils.matchMatcher
import com.calmwolfs.bettermap.utils.StringUtils.unformat
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityZombie
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DungeonUtils {
    private val floorPattern = " §7⏣? §cThe Catacombs §7\\((?<floor>\\w+)\\)".toPattern()
    private val roomIdPattern = "\\d+/\\d+/\\d+ \\w+ (?<roomId>\\S+)".toPattern()

    private var dungeonFloor: String? = null
    private var started = false
    private var boss = false

    private var currentRoomId: String? = null
    private var currentRoomData: RoomData? = null


    //todo in mastermode variable


    private var mimicDead = false
    private var deadBlazes = 0

    private val mimicDeadPattern = "Party > (?:\\\$SKYTILS-DUNGEON-SCORE-MIMIC\\\$|Mimic (?:Killed|Dead|dead)!)".toPattern()

    fun inDungeon() = started
    fun inDungeonRun() = started && !boss
    fun inBossRoom() = boss
    fun getDungeonFloor() = dungeonFloor
    fun getCurrentRoomData() = currentRoomData
    fun isMimicDead() = mimicDead
    fun mimicDeath() { mimicDead = true }

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
            if (!MapUtils.canUpdateRoom()) return
            currentRoomData = RoomDataManager.getRoomData(foundId)
            RoomChangeEvent(currentRoomId, foundId ?: return).postAndCatch()
            currentRoomId = foundId
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ModChatEvent) {
        val floor = getDungeonFloor() ?: return
        val unformatted = event.message.unformat()
        if (event.message == "§e[NPC] §bMort§f: Here, I found this map when I first entered the dungeon.") {
            started = true
            DungeonStartEvent(floor).postAndCatch()
        }

        if (!inBossRoom()) {
            if (unformatted in bossEntryMessages) {
                boss = true
                EnterBossFightEvent(floor).postAndCatch()
            }
        }

        mimicDeadPattern.findMatcher(unformatted) {
            mimicDead = true
        }
    }

    @SubscribeEvent
    fun onEntityDeath(event: EntityDeathEvent) {
        if (event.entityLiving is EntityBlaze) {
            deadBlazes++
            if (deadBlazes >= 10) {
                BetterMapServer.sendDungeonData("blazeDone")
                blazeCompleted()
            }
        }
        if (event.entityLiving is EntityZombie && event.entityLiving.isChild) {
            val mimicEntity = event.entityLiving

            if ((1..4).all { slotNum -> mimicEntity.getEquipmentInSlot(slotNum) == null }) {
                mimicDead = true
                BetterMapServer.sendDungeonData("mimicKilled")
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        dungeonFloor = null
        started = false
        boss = false
        currentRoomId = null
        mimicDead = false
        deadBlazes = 0
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

    fun blazeCompleted() {
        for (room in DungeonMap.uniqueRooms) {
            if (room.roomData()?.name?.lowercase() == "higher or lower") {
                room.roomState = if (room.currentSecrets == room.maxSecrets) RoomState.COMPLETED else RoomState.CLEARED
            }
        }
    }
}