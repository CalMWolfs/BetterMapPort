package com.calmwolfs.bettermap.data.mapdata

data class DungeonRoom(val type: RoomType, val doors: List<DungeonDoor> = listOf(), var roomId: String? = null) {
    var roomState = RoomState.UNOPENED
}
