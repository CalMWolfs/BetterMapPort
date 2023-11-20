package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.ModPair
import com.calmwolfs.bettermap.data.roomdata.RoomDataManager

data class DungeonRoom(var type: RoomType, val components: MutableList<ModPair>, var roomId: String? = null) {
    var roomState = RoomState.ADJACENT
    var currentSecrets = 0
    var maxSecrets = 0

    fun roomData() = RoomDataManager.getRoomData(roomId)
}
