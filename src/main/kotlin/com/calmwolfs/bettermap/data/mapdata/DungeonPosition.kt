package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.ModVector
import com.calmwolfs.bettermap.utils.MapUtils

data class DungeonPosition(val position: ModVector) {
    val mapPos: Pair<Int, Int>
        get() = MapUtils.mapPosFromGridPos(asGridPos(position))

    val mapGrid: Pair<Int, Int>
        get() = asGridPos(position)

    val posInGrid: Pair<Int, Int>
        get() = asPosInGrid(position)

    private fun asGridPos(position: ModVector): Pair<Int, Int> {
        return Pair(
            (position.x.toInt() + DungeonData.ROOM_OFFSET) / DungeonData.ROOM_SIZE,
            (position.y.toInt() + DungeonData.ROOM_OFFSET) / DungeonData.ROOM_SIZE
        )
    }

    private fun asPosInGrid(position: ModVector): Pair<Int, Int> {
        return Pair(
            (position.x.toInt() + DungeonData.ROOM_OFFSET) % DungeonData.ROOM_SIZE,
            (position.y.toInt() + DungeonData.ROOM_OFFSET) % DungeonData.ROOM_SIZE
        )
    }
}
