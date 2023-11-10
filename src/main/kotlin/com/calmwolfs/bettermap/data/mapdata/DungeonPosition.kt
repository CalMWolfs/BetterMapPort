package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.ModVector
import com.calmwolfs.bettermap.utils.MapUtils
import kotlin.math.roundToInt

data class DungeonPosition(val position: ModVector) {
    val mapPos: Pair<Int, Int>
        get() = mapPosFromWorld(position)

    val arrayPos: Pair<Int, Int>
        get() = arrayPosFromWorld(position)

    companion object {
        fun mapPosFromWorld(position: ModVector): Pair<Int, Int> {
            return Pair(
                ((position.x - DungeonData.START_X + DungeonData.ROOM_SIZE) * MapUtils.scaleFactor + MapUtils.spawnPosition.first).roundToInt(),
                ((position.y - DungeonData.START_Y + DungeonData.ROOM_SIZE) * MapUtils.scaleFactor + MapUtils.spawnPosition.second).roundToInt()
            )
        }

        fun arrayPosFromWorld(position: ModVector): Pair<Int, Int> {
            return Pair(
                ((position.x - DungeonData.START_X) / DungeonData.ROOM_SIZE).toInt(),
                ((position.x - DungeonData.START_Y) / DungeonData.ROOM_SIZE).toInt()
            )
        }

        fun arrayPosFromMap(x: Int, y: Int): Pair<Int, Int> {
            return Pair(
                (x - MapUtils.spawnPosition.first) / MapUtils.mapTileSize,
                (y - MapUtils.spawnPosition.second) / MapUtils.mapTileSize
            )
        }
    }
}
