package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.ModPair

data class DungeonRoom(var type: RoomType, val components: MutableList<ModPair>, var roomId: String? = null) {
    var roomState = RoomState.ADJACENT
}
