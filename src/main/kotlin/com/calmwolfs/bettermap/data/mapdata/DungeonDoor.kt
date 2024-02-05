package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.IntPair

data class DungeonDoor(var type: RoomType, val position: IntPair, val horizontal: Boolean)