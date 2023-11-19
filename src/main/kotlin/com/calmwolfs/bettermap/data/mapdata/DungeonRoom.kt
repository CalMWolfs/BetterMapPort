package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.ModPair
import com.calmwolfs.bettermap.data.roomdata.RoomDataManager

data class DungeonRoom(var type: RoomType, val components: MutableList<ModPair>, var roomId: String? = null) {
    var roomState = RoomState.ADJACENT

    fun roomData() = RoomDataManager.getRoomData(roomId)
}
