package com.calmwolfs.bettermap.data.mapdata

data class DungeonRoomData(
    var x: Int,
    var y: Int,
    val height: Int,
    val width: Int,
    val rotation: RoomRotation
)

enum class RoomRotation(val number: Int) {
    NORTH(0),
    EAST(1),
    SOUTH(2),
    WEST(3),
    UNKNOWN(-1),
    ;
}
