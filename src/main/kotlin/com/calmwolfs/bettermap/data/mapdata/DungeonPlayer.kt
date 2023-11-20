package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.data.ModVector
import com.calmwolfs.bettermap.utils.SimpleTimeMark
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class DungeonPlayer(var playerInfo: NetworkPlayerInfo, var username: String) {
    var uuid: String? = null
    var position = ModVector(0, 0, 0)

    var yaw = 0.0

    var currentRoom: DungeonRoom? = null

    var lastLocallyUpdated = SimpleTimeMark.farPast()
    var deaths = 0

    var startSecrets = 0
    var endSecrets = 0

    var minRooms = 0
    var maxRooms = 0

    //todo room data

    private var dungeonClass = DungeonClass.UNKNOWN
    var playerColour = "0:0:0:0:255"

    init {
        checkUpdateUuid()
    }

    private fun updateUsername(name: String) {
        if (name == username) return
        username = name
        uuid = null
        checkUpdateUuid()
    }

    private fun checkUpdateUuid() {
        if (uuid != null) return

        val player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(username) ?: return
        val playerUuid = player.uniqueID.toString()
        uuid = playerUuid

        BetterMapMod.coroutineScope.launch {
            startSecrets = getSecrets(playerUuid, 5.minutes)
            endSecrets = startSecrets // So it doesn't show negative numbers if error later
        }
    }

    private fun updateSecrets() {
        val playerUuid = uuid ?: return

        BetterMapMod.coroutineScope.launch {
            endSecrets = getSecrets(playerUuid, 5.minutes)
        }
    }

    fun getSecretsCollected() = endSecrets - startSecrets

    fun updateColour() {
        // todo if single colour stuff do here

        // todo get from settings
        playerColour = when (dungeonClass) {
            DungeonClass.HEALER -> { "" }
            DungeonClass.TANK -> { "" }
            DungeonClass.MAGE -> { "" }
            DungeonClass.BERSERK -> { "" }
            DungeonClass.ARCHER -> { "" }
            DungeonClass.UNKNOWN -> { playerColour }
        }
    }

    fun updateTabInfo(name: String, clazz: String) {
        updateUsername(name)
        dungeonClass = DungeonClass.fromTabWord(clazz)
    }

    private suspend fun getSecrets(uuid: String, cacheTime: Duration): Int {
        // todo some global cache uuid -> simple time mark + secret count, cant be reg cache cus yeah so to pair?

        return  -1
    }
}
