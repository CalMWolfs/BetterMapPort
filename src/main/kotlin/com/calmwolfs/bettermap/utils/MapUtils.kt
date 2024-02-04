package com.calmwolfs.bettermap.utils

import com.calmwolfs.bettermap.data.ModPair
import com.calmwolfs.bettermap.data.ModVector
import com.calmwolfs.bettermap.data.asGridPos
import com.calmwolfs.bettermap.data.connection.BetterMapServer
import com.calmwolfs.bettermap.data.mapdata.DungeonData
import com.calmwolfs.bettermap.data.mapdata.DungeonData.DOOR_SIZE
import com.calmwolfs.bettermap.data.mapdata.DungeonDoor
import com.calmwolfs.bettermap.data.mapdata.DungeonMap
import com.calmwolfs.bettermap.data.mapdata.DungeonRoom
import com.calmwolfs.bettermap.data.mapdata.MapColourArray
import com.calmwolfs.bettermap.data.mapdata.MapTeam
import com.calmwolfs.bettermap.data.mapdata.RoomState
import com.calmwolfs.bettermap.data.mapdata.RoomType
import com.calmwolfs.bettermap.data.roomdata.RoomDataManager
import com.calmwolfs.bettermap.events.ActionBarUpdateEvent
import com.calmwolfs.bettermap.events.MapUpdateEvent
import com.calmwolfs.bettermap.events.ModTickEvent
import com.calmwolfs.bettermap.events.RoomChangeEvent
import com.calmwolfs.bettermap.events.TablistUpdateEvent
import com.calmwolfs.bettermap.events.WorldChangeEvent
import com.calmwolfs.bettermap.utils.JsonUtils.getIntOrValue
import com.calmwolfs.bettermap.utils.JsonUtils.getStringOrNull
import com.calmwolfs.bettermap.utils.JsonUtils.getStringOrValue
import com.calmwolfs.bettermap.utils.StringUtils.findMatcher
import com.calmwolfs.bettermap.utils.StringUtils.matchMatcher
import com.calmwolfs.bettermap.utils.StringUtils.unformat
import com.calmwolfs.bettermap.utils.Vec4bUtils.mapX
import com.calmwolfs.bettermap.utils.Vec4bUtils.mapY
import com.calmwolfs.bettermap.utils.Vec4bUtils.yaw
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemMap
import net.minecraft.util.Vec4b
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

object MapUtils {
    private var savedMap = MapColourArray.empty()

    var spawnTilePosition: ModPair = ModPair(-1, -1)
    var topLeftTilePos: ModPair = ModPair(-1, -1)
    var mapTileCount: ModPair = ModPair(-1, -1)

    private var tileSize = 0
    private var mapCalibrated = false

    private var identifiedPuzzleCount = 0

    private var scaleFactor = 0.0
    private var bloodOpen = false

    fun isMapCalibrated() = mapCalibrated
    fun bloodOpened() = bloodOpen

    private val puzzleCountPattern = "Puzzles: \\((?<puzzles>\\d)\\)".toPattern()
    private val puzzleInfoPattern = "\\s(?<name>.*): \\[(?<status>.)]".toPattern()
    private val secretsPattern = "(?<current>\\d+)/(?<max>\\d+) Secrets".toPattern()

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
        if (!event.repeatSeconds(1)) return
        if (!isMapCalibrated()) return
        val decorations = mapData.mapDecorations ?: return
        updatePlayersFromMap(decorations)
    }

    @SubscribeEvent
    fun onRoomChange(event: RoomChangeEvent) {
        if (DungeonMap.foundRoomIds.contains(event.newRoomId)) return
        val gridPosition = LocationUtils.playerLocation().asGridPos()

        val currentRoom = getRoom(gridPosition) ?: return
        if (currentRoom.type == RoomType.UNKNOWN) return

        if (currentRoom.roomId != null || currentRoom.type == RoomType.UNKNOWN) return

        currentRoom.roomId = event.newRoomId
        DungeonMap.foundRoomIds.add(event.newRoomId)

        BetterMapServer.sendDungeonData(
            "roomId",
            "x" to gridPosition.first,
            "y" to gridPosition.second,
            "roomId" to event.newRoomId
        )
    }

    fun updateRoomId(data: JsonObject) {
        val currentRoom = getRoom(ModPair(data.getIntOrValue("x"), data.getIntOrValue("x"))) ?: return
        if (currentRoom.type == RoomType.UNKNOWN) return
        currentRoom.roomId ?: return

        val roomId = data.getStringOrNull("roomId") ?: return

        currentRoom.roomId = roomId
        ChatUtils.chat("Updated Room Id")
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
    fun onActionBarUpdate(event: ActionBarUpdateEvent) {
        if (!DungeonUtils.inDungeonRun()) return

        secretsPattern.findMatcher(event.actionBar.unformat()) {
            val current = group("current").toInt()
            val max = group("max").toInt()

            val gridPosition = LocationUtils.playerLocation().asGridPos()

            val currentRoom = getRoom(gridPosition) ?: return
            if (currentRoom.type == RoomType.UNKNOWN) return

            if (currentRoom.currentSecrets == current && currentRoom.maxSecrets == max) return
            currentRoom.currentSecrets = current
            currentRoom.maxSecrets = max

            if (currentRoom.roomState == RoomState.CLEARED && currentRoom.currentSecrets >= currentRoom.maxSecrets) {
                currentRoom.roomState = RoomState.COMPLETED
            }

            BetterMapServer.sendDungeonData(
                "roomSecrets",
                "min" to current,
                "max" to max,
                "x" to gridPosition.first,
                "y" to gridPosition.second
            )
        }
    }

    fun updateSecrets(data: JsonObject) {
        val currentRoom = getRoom(ModPair(data.getIntOrValue("x"), data.getIntOrValue("x"))) ?: return
        if (currentRoom.type == RoomType.UNKNOWN) return

        val current = data.getIntOrValue("min")
        val max = data.getIntOrValue("max")
        if (current == -1 || max == -1) return

        if (currentRoom.currentSecrets == current && currentRoom.maxSecrets == max) return
        currentRoom.currentSecrets = current
        currentRoom.maxSecrets = max
        ChatUtils.chat("Updated Secret Data")
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
        DungeonMap.foundRoomIds.clear()
        savedMap = MapColourArray.empty()

        spawnTilePosition = ModPair(-1, -1)
        topLeftTilePos = ModPair(-1, -1)
        mapTileCount = ModPair(-1, -1)

        identifiedPuzzleCount = 0

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
            scaleFactor = mapTileSize / DungeonData.ROOM_SIZE.toDouble()

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
        for (y in 0 until mapTileCount.second) {
            for (x in 0 until mapTileCount.first) {
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

        val leftRoom = if (getRoomType(mapPosFromGridPos(position, ModPair(0, DOOR_SIZE))) == RoomType.NORMAL) {
            getRoom(ModPair(position.first - 1, position.second))
        } else null
        val topRoom = if (getRoomType(mapPosFromGridPos(position, ModPair(DOOR_SIZE, 0))) == RoomType.NORMAL) {
            getRoom(ModPair(position.first, position.second - 1))
        } else null

        /**
         * Checking to see if the room to the right connects both left and up for that one case of L shaped rooms
         */
        val topRightRoom = if (getRoomType(mapPosFromGridPos(ModPair(position.first + 1, position.second), ModPair(DOOR_SIZE, 0))) == RoomType.NORMAL &&
            getRoomType(mapPosFromGridPos(ModPair(position.first + 1, position.second), ModPair(0, DOOR_SIZE))) == RoomType.NORMAL) {
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

    private fun updatePlayersFromMap(decorations: MutableMap<String, Vec4b>) {
        var i = 0
        val mapTeam = MapTeam.getMapPlayers()

        for (decoration in decorations) {
            if (i > mapTeam.size) return

            val playerData = mapTeam[i] ?: continue
            if (playerData.username.lowercase() in MapTeam.getDeadPlayers()) continue

            if (playerData.lastLocallyUpdated.passedSince() < 1500.milliseconds) continue

            playerData.yaw = decoration.value.yaw()

            playerData.position = mapPosToModVector(ModPair(decoration.value.mapX(), decoration.value.mapY()))

            i++
        }
    }

    @SubscribeEvent
    fun onTablistUpdate(event: TablistUpdateEvent) {
        if (!DungeonUtils.inDungeonRun()) return

        var puzzleCount = -1
        val puzzleNames = mutableListOf<String>()
        val identifiedPuzzles = mutableListOf<String>()

        val puzzleIndex = event.tablist.indexOfFirst  { line ->
            puzzleCountPattern.matchMatcher(line.unformat()) { puzzleCount = group("puzzles").toInt() } != null
        }
        if (puzzleIndex == -1 || puzzleCount < 1) return

        val sublist = event.tablist.subList(puzzleIndex + 1, puzzleIndex + puzzleCount + 1)
        loop@ for (line in sublist) {
            puzzleInfoPattern.findMatcher(line.unformat()) {
                val puzzleName = group("name")
                val puzzleStatus = group("status")

                if (puzzleName == "???") continue@loop
                puzzleNames.add(puzzleName)

                if (puzzleStatus != "✖") continue@loop
                for (room in DungeonMap.uniqueRooms) {
                    if (room.roomData()?.name?.lowercase() == puzzleName.lowercase()) {
                        room.roomState = RoomState.FAILED
                    }
                }
            }
        }

        puzzleCount = 0
        for (room in DungeonMap.uniqueRooms) {
            if (room.type == RoomType.PUZZLE && room.roomState != RoomState.ADJACENT) {
                if (room.roomId != null) {
                    identifiedPuzzles.add(room.roomData()?.name ?: "???")
                }
                puzzleCount++
            }
        }

        if (puzzleNames.size <= identifiedPuzzleCount) return
        if (puzzleNames.size != puzzleCount) return

        for (y in 0 until mapTileCount.second) {
            for (x in 0 until mapTileCount.first) {
                val room = getRoom(ModPair(x, y)) ?: continue
                if (room.type == RoomType.PUZZLE && room.roomId == null) {
                    val puzzleName = puzzleNames.removeAt(0)
                    val roomIds = RoomDataManager.getRoomIdFromName(puzzleName)

                    if (roomIds.isEmpty()) continue
                    room.roomId = roomIds[0]
                    DungeonMap.foundRoomIds.addAll(roomIds)
                }
            }
        }
        identifiedPuzzleCount = puzzleNames.size
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

    fun worldPosFromMapPos(mapPosition: ModPair): ModPair {
        return ModPair(
            ((mapPosition.first - topLeftTilePos.first) / scaleFactor).toInt() - DungeonData.ROOM_OFFSET,
            ((mapPosition.second - topLeftTilePos.second) / scaleFactor).toInt() - DungeonData.ROOM_OFFSET
        )
    }

    fun mapPosToModVector(mapPosition: ModPair, yValue: Int = 0): ModVector {
        val worldPos = worldPosFromMapPos(mapPosition)

        return ModVector(worldPos.first, yValue, worldPos.second)
    }

    private fun getRoomType(location: ModPair): RoomType {
        return RoomType.fromColour(savedMap[location])
    }

    fun getRoom(location: ModPair) = DungeonMap.dungeonRooms[location]

    fun getCurrentRoom() = getRoom(LocationUtils.playerLocation().asGridPos())
}