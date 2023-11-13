package com.calmwolfs.bettermap.utils

import com.calmwolfs.bettermap.data.ModPair
import com.calmwolfs.bettermap.data.mapdata.DungeonData
import com.calmwolfs.bettermap.data.mapdata.DungeonData.DOOR_SIZE
import com.calmwolfs.bettermap.data.mapdata.DungeonDoor
import com.calmwolfs.bettermap.data.mapdata.DungeonMap
import com.calmwolfs.bettermap.data.mapdata.DungeonRoom
import com.calmwolfs.bettermap.data.mapdata.MapColourArray
import com.calmwolfs.bettermap.data.mapdata.RoomState
import com.calmwolfs.bettermap.data.mapdata.RoomType
import com.calmwolfs.bettermap.events.MapUpdateEvent
import com.calmwolfs.bettermap.events.ModTickEvent
import com.calmwolfs.bettermap.events.WorldChangeEvent
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemMap
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MapUtils {
    private var savedMap = MapColourArray.empty()

    var spawnTilePosition: ModPair = ModPair(-1, -1)
    var topLeftTilePos: ModPair = ModPair(-1, -1)
    var mapTileCount: ModPair = ModPair(-1, -1)

    private var tileSize = 0
    private var mapCalibrated = false

    var scaleFactor = 0.0
    private var bloodOpen = false

    fun isMapCalibrated() = mapCalibrated
    fun bloodOpened() = bloodOpen

    val mapTileSize: Int
        get() = tileSize + DOOR_SIZE * 2

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (!DungeonUtils.inDungeonRun()) return
        val mapSlot = InventoryUtils.getMapSlot() ?: return
        val mapSlotItem = mapSlot.item

        if (mapSlotItem !is ItemMap) return
        val mapData = mapSlotItem.getMapData(mapSlot, Minecraft.getMinecraft().theWorld) ?: return

        val pixelData: Array<Array<Int>> = Array(128) { Array(128) { 0 } }

        val mapColours = mapData.colors

        for (x in 0 until 128) {
            for (y in 0 until 128) {
                pixelData[x][y] = mapColours[x + y * 128].toInt()
            }
        }
        val colourArray = MapColourArray(pixelData)

        if (colourArray != savedMap) {
            savedMap = colourArray
            MapUpdateEvent().postAndCatch()
        }
    }

    @SubscribeEvent
    fun onMapUpdate(event: MapUpdateEvent) {
        if (!mapCalibrated) {
            findEntranceCorner()
        }
        if (mapCalibrated) {
            getDungeonRooms()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        clearMap()
    }

    private fun clearMap() {
        mapCalibrated = false
        DungeonMap.dungeonRooms.clear()
        DungeonMap.uniqueRooms.clear()
        DungeonMap.dungeonDoors.clear()
        DungeonMap.witherDoors.clear()
        savedMap = MapColourArray.empty()

        spawnTilePosition = ModPair(-1, -1)
        topLeftTilePos = ModPair(-1, -1)
        mapTileCount = ModPair(-1, -1)

        tileSize = 0
        bloodOpen = false

        scaleFactor = 0.0
    }

    private fun findEntranceCorner() {
        var spawnMapPosition = ModPair(-1, -1)
        var tileCount = 0

        for (x in savedMap.getColours().indices) {
            for (y in savedMap.getColours()[x].indices) {
                when {
                    spawnMapPosition != ModPair(-1, -1) && savedMap[x, y] != RoomType.SPAWN.roomColour -> {
                        tileSize = tileCount
                        calibrateMap(spawnMapPosition)
                        return
                    }
                    spawnMapPosition != ModPair(-1, -1) -> {
                        tileCount++
                    }
                    savedMap[x, y] == RoomType.SPAWN.roomColour -> {
                        spawnMapPosition = ModPair(x, y)
                        tileCount++
                    }
                }
            }
        }
    }

    private fun calibrateMap(spawnMapPosition: ModPair) {
        mapCalibrated = if (tileSize == 16 || tileSize == 18) {
            scaleFactor = tileSize / DungeonData.ROOM_SIZE.toDouble()

            val (startPosX, tileCountX) = getMapStartPositionAndSize(spawnMapPosition.first)
            val (startPosY, tileCountY) = getMapStartPositionAndSize(spawnMapPosition.second)

            topLeftTilePos = ModPair(startPosX, startPosY)
            mapTileCount = ModPair(tileCountX, tileCountY)

            spawnTilePosition = gridPosFromMapPos(spawnMapPosition)
            true
        } else {
            false
        }
    }

    /**
     * Returns a pair with the start pos on the map and the amount of tiles in that column or row
     */
    private fun getMapStartPositionAndSize(mapCoordinate: Int): ModPair {
        return if (tileSize == 16) {
            if (mapCoordinate % 2 == 0) ModPair(14, 5) else ModPair(3, 6)
        } else {
            if (mapCoordinate % 2 == 0) ModPair(20, 4) else ModPair(9, 5)
        }
    }

    private fun getDungeonRooms() {
        for (y in 0 until mapTileCount.first) {
            for (x in 0 until mapTileCount.second) {
                val location = mapPosFromGridPos(ModPair(x, y), ModPair(DOOR_SIZE, DOOR_SIZE))

                when (val roomType = getRoomType(location)) {
                    RoomType.UNKNOWN -> continue
                    RoomType.NORMAL -> {
                        processNormalRoom(ModPair(x, y))
                    }
                    else -> {
                        processSpecialRoom(roomType, ModPair(x, y))
                    }
                }
                checkRoomStatus(ModPair(x, y))
                checkRoomDoors(ModPair(x, y))
            }
        }
    }

    private fun processSpecialRoom(roomType: RoomType, position: ModPair) {
        if (roomType == RoomType.BLOOD) bloodOpen = true

        val currentRoom = getRoom(position)
        if (currentRoom == null) {
            val room = DungeonRoom(roomType, mutableListOf(position), null)
            room.roomState = if (roomType == RoomType.UNOPENED) RoomState.ADJACENT else RoomState.OPENED
            DungeonMap.dungeonRooms[position] = room
            DungeonMap.uniqueRooms.add(room)
        } else {
            if (roomType != currentRoom.type) {
                currentRoom.type = roomType
                currentRoom.roomState = if (roomType == RoomType.UNOPENED) RoomState.ADJACENT else RoomState.OPENED
            }
        }
    }

    private fun processNormalRoom(position: ModPair) {
        val currentRoom = getRoom(position)

        val leftRoom = if (getRoomType(mapPosFromGridPos(position, ModPair(DOOR_SIZE - 1, DOOR_SIZE))) == RoomType.NORMAL) {
            getRoom(ModPair(position.first - 1, position.second))
        } else null
        val topRoom = if (getRoomType(mapPosFromGridPos(position, ModPair(DOOR_SIZE, DOOR_SIZE - 1))) == RoomType.NORMAL) {
            getRoom(ModPair(position.first, position.second - 1))
        } else null

        /**
         * Checking to see if the room to the right connects both left and up for that one case of L shaped rooms
         */
        val topRightRoom = if (getRoomType(mapPosFromGridPos(ModPair(position.first + 1, position.second), ModPair(DOOR_SIZE, DOOR_SIZE - 1))) == RoomType.NORMAL &&
            getRoomType(mapPosFromGridPos(ModPair(position.first + 1, position.second + 1), ModPair(DOOR_SIZE - 1, DOOR_SIZE))) == RoomType.NORMAL) {
            getRoom(ModPair(position.first + 1, position.second - 1))
        } else null

        // new room needs adding
        if (currentRoom == null && leftRoom == null && topRoom == null && topRightRoom == null) {
            val room = DungeonRoom(RoomType.NORMAL, mutableListOf(position), null)
            room.roomState = RoomState.OPENED
            DungeonMap.dungeonRooms[position] = room
            DungeonMap.uniqueRooms.add(room)
        }

        // room already added but needed status changing
        if (currentRoom != null && currentRoom.type != RoomType.NORMAL) {
            currentRoom.type = RoomType.NORMAL
            currentRoom.roomState = RoomState.OPENED
        }
        // merging left
        if (leftRoom != null && currentRoom != leftRoom && !leftRoom.components.contains(position)) {
            DungeonMap.uniqueRooms.remove(currentRoom)
            leftRoom.components.add(position)
            DungeonMap.dungeonRooms[position] = leftRoom
        }
        // merging up
        if (topRoom != null && currentRoom != topRoom && !topRoom.components.contains(position)) {
            DungeonMap.uniqueRooms.remove(currentRoom)
            topRoom.components.add(position)
            DungeonMap.dungeonRooms[position] = topRoom
        }
        // merging up and to right for that one orientation of L shaped rooms
        if (topRightRoom != null && currentRoom != topRightRoom && !topRightRoom.components.contains(position)) {
            DungeonMap.uniqueRooms.remove(currentRoom)
            topRightRoom.components.add(position)
            DungeonMap.dungeonRooms[position] = topRightRoom
        }
    }

    private fun checkRoomStatus(position: ModPair) {
        val offset = ModPair(mapTileSize / 2, mapTileSize / 2)
        val roomCenterColour = savedMap[mapPosFromGridPos(position, offset)]
        val checkmarkRoom = getRoom(position)

        // white tick
        if (checkmarkRoom != null && roomCenterColour == 34 && checkmarkRoom.roomState != RoomState.CLEARED) {
            checkmarkRoom.roomState = RoomState.CLEARED
        }
        // green tick
        if (checkmarkRoom != null && roomCenterColour == 30 && checkmarkRoom.roomState != RoomState.COMPLETED && checkmarkRoom.type != RoomType.SPAWN) {
            checkmarkRoom.roomState = RoomState.COMPLETED
        }
        // red cross
        if (checkmarkRoom != null && roomCenterColour == 18 && checkmarkRoom.roomState != RoomState.FAILED && checkmarkRoom.type != RoomType.BLOOD) {
            checkmarkRoom.roomState = RoomState.FAILED
        }
    }

    private fun checkRoomDoors(position: ModPair) {
        // top door
        if (getRoomType(mapPosFromGridPos(position, ModPair(DOOR_SIZE, 0))) == RoomType.UNKNOWN &&
            getRoomType(mapPosFromGridPos(position, ModPair(mapTileSize / 2, 0))) != RoomType.UNKNOWN) {

            val doorType = getRoomType(mapPosFromGridPos(position, ModPair(mapTileSize / 2, 0)))
            checkAndUpdateDoor(position, true, doorType)
        }

        // left door
        if (getRoomType(mapPosFromGridPos(position, ModPair(0, DOOR_SIZE))) == RoomType.UNKNOWN &&
            getRoomType(mapPosFromGridPos(position, ModPair(0, mapTileSize / 2))) != RoomType.UNKNOWN) {

            val doorType = getRoomType(mapPosFromGridPos(position, ModPair(0, mapTileSize / 2)))
            checkAndUpdateDoor(position, false, doorType)
        }
    }

    private fun checkAndUpdateDoor(position: ModPair, horizontal: Boolean, doorType: RoomType) {
        val door = DungeonMap.dungeonDoors.firstOrNull { it.position == position && it.horizontal == horizontal }
        if (door == null) {
            val newDoor = DungeonDoor(doorType, position, horizontal)
            DungeonMap.dungeonDoors.add(newDoor)
            if (newDoor.type == RoomType.WITHER || newDoor.type == RoomType.BLOOD) {
                DungeonMap.witherDoors.add(newDoor)
            }
        } else if (door.type != doorType) {
            if (doorType == RoomType.WITHER || doorType == RoomType.BLOOD) {
                DungeonMap.witherDoors.add(door)
            } else if (door in DungeonMap.witherDoors) {
                DungeonMap.witherDoors.remove(door)
            }
            door.type = doorType
        }
    }

    fun gridPosFromMapPos(mapPosition: ModPair) : ModPair {
        return ModPair(
            (mapPosition.first - topLeftTilePos.first) / mapTileSize,
            (mapPosition.second - topLeftTilePos.second) / mapTileSize
        )
    }

    fun mapPosFromGridPos(gridPosition: ModPair, offset: ModPair = ModPair(0, 0)) : ModPair {
        return ModPair(
            gridPosition.first * mapTileSize + topLeftTilePos.first + offset.first,
            gridPosition.second * mapTileSize + topLeftTilePos.second + offset.second
        )
    }

    private fun getRoomType(location: ModPair): RoomType {
        return RoomType.fromColour(savedMap[location])
    }

    private fun getRoom(location: ModPair) = DungeonMap.dungeonRooms[location]
}