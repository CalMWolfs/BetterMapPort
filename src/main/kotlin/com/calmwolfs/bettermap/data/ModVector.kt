package com.calmwolfs.bettermap.data

import com.calmwolfs.bettermap.data.mapdata.DungeonData
import net.minecraft.entity.Entity
import kotlin.math.floor

data class ModVector(
    val x: Double,
    val y: Double,
    val z: Double,
)  {
    constructor(intList: List<Int>) : this(
        x = intList.getOrNull(0)?.toDouble() ?: -1.0,
        y = intList.getOrNull(1)?.toDouble() ?: -1.0,
        z = intList.getOrNull(2)?.toDouble() ?: -1.0
    )
    constructor(x: Int, y: Int, z: Int) : this(x.toDouble(), y.toDouble(), z.toDouble())
}

fun Entity.getModVector(): ModVector = ModVector(posX, posY, posZ)

fun ModVector.asGridPos(): ModPair {
    return ModPair(
        (this.x.toInt() + DungeonData.ROOM_OFFSET) / DungeonData.ROOM_SIZE,
        (this.z.toInt() + DungeonData.ROOM_OFFSET) / DungeonData.ROOM_SIZE
    )
}

fun ModVector.asPosInGrid(): ModPair {
    return ModPair(
        (this.x.toInt() + DungeonData.ROOM_OFFSET) % DungeonData.ROOM_SIZE,
        (this.z.toInt() + DungeonData.ROOM_OFFSET) % DungeonData.ROOM_SIZE
    )
}

fun ModVector.toRoomTopCorner(): ModPair {
    return ModPair(
        floor((this.x + 8) / 32).toInt() * 32 - 8,
        floor((this.z + 8) / 32).toInt() * 32 - 8
    )
}
