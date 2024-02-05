package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.IntPair

object DungeonMap {
    val dungeonRooms = mutableMapOf<IntPair, DungeonRoom>()
    val uniqueRooms = mutableListOf<DungeonRoom>()
    val dungeonDoors = mutableListOf<DungeonDoor>()
    val witherDoors = mutableListOf<DungeonDoor>()
    val foundRoomIds = mutableListOf<String>()
}