package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.ModPair

object DungeonMap {
    val dungeonRooms = mutableMapOf<ModPair, DungeonRoom>()
    val uniqueRooms = mutableListOf<DungeonRoom>()
    val dungeonDoors = mutableListOf<DungeonDoor>()
    val witherDoors = mutableListOf<DungeonDoor>()

}