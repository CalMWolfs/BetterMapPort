package com.calmwolfs.bettermap.utils

import com.calmwolfs.bettermap.data.IntPair
import com.calmwolfs.bettermap.data.WorldData
import com.calmwolfs.bettermap.data.mapdata.DungeonData
import com.calmwolfs.bettermap.data.mapdata.DungeonRoomData
import com.calmwolfs.bettermap.data.mapdata.RoomRotation
import com.calmwolfs.bettermap.data.roomdata.RoomShape
import com.calmwolfs.bettermap.data.toRoomTopCorner

object DungeonMapUtils {
    private fun getRoomWorldData(): DungeonRoomData {
        var (x, z) = LocationUtils.playerLocation().toRoomTopCorner()

        var width = DungeonData.ROOM_SIZE - DungeonData.DOOR_SIZE
        var height = DungeonData.ROOM_SIZE - DungeonData.DOOR_SIZE

        val roofY = getRoofHeight(x, z)

        while (!WorldData.isBlockAir(x - 1, roofY, z)) {
            x -= DungeonData.ROOM_SIZE
            width += DungeonData.ROOM_SIZE
        }
        while (!WorldData.isBlockAir(x, roofY, z - 1)) {
            z -= DungeonData.ROOM_SIZE
            height += DungeonData.ROOM_SIZE
        }
        while (!WorldData.isBlockAir(x - 1, roofY, z)) { // Second iteration in case of L shape
            x -= DungeonData.ROOM_SIZE
            width += DungeonData.ROOM_SIZE
        }

        while (!WorldData.isBlockAir(x + width + 1, roofY, z)) {
            width += DungeonData.ROOM_SIZE
        }
        while (!WorldData.isBlockAir(x, roofY, z + height + 1)) {
            height += DungeonData.ROOM_SIZE
        }

        while (!WorldData.isBlockAir(x + width, roofY, z + height + 1)) { // in case of L shape
            height += DungeonData.ROOM_SIZE
        }
        while (!WorldData.isBlockAir(x + width + 1, roofY, z + height)) { // in case of L shape
            width += DungeonData.ROOM_SIZE
        }

        while (!WorldData.isBlockAir(x + width, roofY, z - 1) && // in case of L shape
            !WorldData.isBlockAir(x + width, roofY, z - 1 + if (height == 30) 0 else 32)) {
            z -= DungeonData.ROOM_SIZE
            height += DungeonData.ROOM_SIZE
        }
        while (!WorldData.isBlockAir(x - 1, roofY, z + height) && // in case of L shape
            !WorldData.isBlockAir(x - 1 + if (width == 30) 0 else 32, roofY, z + height)) {
            x -= DungeonData.ROOM_SIZE
            width += DungeonData.ROOM_SIZE
        }

        while (width > 30 && height > 30 && // in case of L shape
            WorldData.isBlockAir(x + DungeonData.ROOM_SIZE - 1, roofY, z + DungeonData.ROOM_SIZE - 1) &&
            !WorldData.isBlockAir(x + DungeonData.ROOM_SIZE - 1, roofY, z + DungeonData.ROOM_SIZE) &&
            !WorldData.isBlockAir(x + DungeonData.ROOM_SIZE, roofY, z + DungeonData.ROOM_SIZE - 1)) {
            x += DungeonData.ROOM_SIZE
        }

        val rotation = getRotation(x, z, width, height, roofY)
        return DungeonRoomData(x, z, height, width, rotation)
    }

    fun getRoomXYWorld(): IntPair {
        val roomData = getRoomWorldData()
        return IntPair(roomData.x, roomData.y)
    }

    private fun getRotation(x: Int, z: Int, width: Int, height: Int, roofY: Int): RoomRotation {
        val currentRoomData = DungeonUtils.getCurrentRoomData() ?: return RoomRotation.UNKNOWN
        if (currentRoomData.shape != RoomShape.L_SHAPE) {
            if (WorldData.getBlockIdAt(x, roofY, z) == 11) return RoomRotation.NORTH
            if (WorldData.getBlockIdAt(x + width, roofY, z) == 11) return RoomRotation.EAST
            if (WorldData.getBlockIdAt(x + width, roofY, z + height) == 11) return RoomRotation.SOUTH
            if (WorldData.getBlockIdAt(x, roofY, z + height) == 11) return RoomRotation.WEST
        } else {
            val cornerX = x + width / 2
            val cornerZ = z + height / 2

            val one = WorldData.isBlockAir(cornerX + 1, roofY, cornerZ)
            val two = WorldData.isBlockAir(cornerX - 1, roofY, cornerZ)
            val three = WorldData.isBlockAir(cornerX, roofY, cornerZ + 1)
            val four = WorldData.isBlockAir(cornerX, roofY, cornerZ - 1)

            if (one && three) return RoomRotation.NORTH
            if (two && three) return RoomRotation.EAST
            if (one && four) return RoomRotation.WEST
            if (two && four) return RoomRotation.SOUTH
        }

        return RoomRotation.UNKNOWN
    }

    private fun getRoofHeight(x: Int, z: Int): Int {
        var y = 255
        while (y > 0 && WorldData.getBlockIdAt(x, y, z) == 0) y--

        return y
    }
}
