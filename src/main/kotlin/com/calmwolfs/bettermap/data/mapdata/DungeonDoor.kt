package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.ModPair

data class DungeonDoor(var type: RoomType, val position: ModPair, val horizontal: Boolean)