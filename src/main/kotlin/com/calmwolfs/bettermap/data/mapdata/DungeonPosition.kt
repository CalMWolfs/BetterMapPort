package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.ModPair
import com.calmwolfs.bettermap.data.ModVector
import com.calmwolfs.bettermap.data.mapdata.DungeonData.ROOM_OFFSET
import com.calmwolfs.bettermap.data.mapdata.DungeonData.ROOM_SIZE

data class DungeonPosition(val position: ModVector) {
    // this gets the corner not the actual pos
//    val mapPos: Pair<Int, Int>
//        get() = MapUtils.mapPosFromGridPos(asGridPos(position))

    val mapGrid: ModPair
        get() = asGridPos(position)

    val posInGrid: ModPair
        get() = asPosInGrid(position)

    private fun asGridPos(position: ModVector): ModPair {
        return ModPair(
            (position.x.toInt() + ROOM_OFFSET) / ROOM_SIZE,
            (position.y.toInt() + ROOM_OFFSET) / ROOM_SIZE
        )
    }

    private fun asPosInGrid(position: ModVector): ModPair {
        return ModPair(
            (position.x.toInt() + ROOM_OFFSET) % ROOM_SIZE,
            (position.y.toInt() + ROOM_OFFSET) % ROOM_SIZE
        )
    }
}
