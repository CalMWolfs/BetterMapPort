package com.calmwolfs.bettermap.data.roomdata

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.commands.CopyErrorCommand
import com.calmwolfs.bettermap.config.ConfigFileType
import com.calmwolfs.bettermap.events.ConfigLoadEvent
import com.calmwolfs.bettermap.utils.ApiUtils
import com.calmwolfs.bettermap.utils.ChatUtils
import com.calmwolfs.bettermap.utils.ModUtils
import com.calmwolfs.bettermap.utils.SimpleTimeMark
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

object RoomDataManager {
    private val gson get() = BetterMapMod.configManager.gson
    private val storage get() = BetterMapMod.roomData

    private val tempRoomData = mutableListOf<RoomDataApi>()
    private val tempProcessedRoomData = mutableListOf<RoomData>()
    private var lastLoad = SimpleTimeMark.farPast()

    private val loadedRooms = mutableMapOf<String, RoomData>()

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        loadData()
    }

    fun loadData(isCommand: Boolean = false) {
        if (lastLoad.passedSince() < 30.seconds) {
            ChatUtils.chat("§6[BetterMap] §7Please wait a bit before attempting to load data from the API.")
            return
        }
        lastLoad = SimpleTimeMark.now()
        BetterMapMod.coroutineScope.launch {
            try {
                val apiData = ApiUtils.loadFromAPI().toString()
                tempRoomData.clear()
                tempRoomData.addAll(gson.fromJson(apiData, object : TypeToken<List<RoomDataApi>>() {}.type))
                tempProcessedRoomData.clear()

                for (room in tempRoomData) {
                    tempProcessedRoomData.add(room.toRoomData())
                }

                storage.roomData = tempProcessedRoomData
                BetterMapMod.configManager.saveConfig(ConfigFileType.ROOM_DATA, "Update room data")
            } catch (e: Exception) {
                CopyErrorCommand.logError(e, "Error while fetching and processing api room data!")
                if (storage.roomData != null && storage.roomData.isNotEmpty()) {
                    ModUtils.warning("There is still saved room data but it may be outdated!")
                }
            }
            if (storage.roomData == null || storage.roomData.isEmpty()) {
                ModUtils.warning("No saved room data, some features won't work!")
            }

            loadedRooms.clear()
            for (room in storage.roomData) {
                val roomIds = room.ids
                for (roomId in roomIds) {
                    loadedRooms[roomId] = room
                }
            }

            if (isCommand) ChatUtils.chat("§6[BetterMap] §7Refreshed rooms from the api")
        }
    }
}