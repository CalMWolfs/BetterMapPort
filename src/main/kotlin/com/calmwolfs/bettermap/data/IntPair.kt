package com.calmwolfs.bettermap.data

import com.calmwolfs.bettermap.data.mapdata.DungeonData

data class IntPair(val x: Int = -1, val y: Int = -1) {

    override fun toString(): String {
        return "(x: $x, y: $y)"
    }

    operator fun plus(other: IntPair) = IntPair(this.x + other.x, this.y + other.y)
    operator fun minus(other: IntPair) = IntPair(this.x - other.x, this.y - other.y)
}

fun IntPair.asGridPos(): IntPair {
    return IntPair(
        (this.x + DungeonData.ROOM_OFFSET) / DungeonData.ROOM_SIZE,
        (this.y + DungeonData.ROOM_OFFSET) / DungeonData.ROOM_SIZE
    )
}