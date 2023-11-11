package com.calmwolfs.bettermap.utils

import com.calmwolfs.bettermap.data.mapdata.DungeonData
import com.calmwolfs.bettermap.data.mapdata.RoomType
import com.calmwolfs.bettermap.events.MapUpdateEvent
import com.calmwolfs.bettermap.events.ModTickEvent
import com.calmwolfs.bettermap.events.WorldChangeEvent
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemMap
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MapUtils {
    private var savedMap: Array<Array<Int>> = Array(128) { Array(128) { 0 } }

    var spawnTilePosition: Pair<Int, Int> = Pair(-1, -1)
    var topLeftTilePos: Pair<Int, Int> = Pair(-1, -1)

    private var tileSize = 0
    private var mapCalibrated = false

    var scaleFactor = 0.0

    fun isMapCalibrated() = mapCalibrated

    val mapTileSize: Int
        get() = tileSize + 4

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (!DungeonUtils.inDungeonRun()) return
        val mapSlot = InventoryUtils.getMapSlot() ?: return
        val mapSlotItem = mapSlot.item

        if (mapSlotItem !is ItemMap) return
        val mapData = mapSlotItem.getMapData(mapSlot, Minecraft.getMinecraft().theWorld) ?: return

        val pixelData: Array<Array<Int>> = Array(128) { Array(128) { 0 } }

        for (x in 0 until 128) {
            for (y in 0 until 128) {
                val color = mapData.colors[x + y * 128].toInt()
                pixelData[x][y] = color
            }
        }
        if (!pixelData.contentEquals(savedMap)) {
            savedMap = pixelData
            MapUpdateEvent(savedMap).postAndCatch()
        }
    }

    @SubscribeEvent
    fun onMapUpdate(event: MapUpdateEvent) {
        if (!mapCalibrated) {
            findEntranceCorner()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        clearMap()
    }

    private fun clearMap() {
        mapCalibrated = false
    }

    private fun findEntranceCorner() {
        var spawnMapPosition = Pair(-1, -1)
        var tileCount = 0

        for (x in savedMap.indices) {
            for (y in savedMap[x].indices) {
                when {
                    spawnMapPosition != Pair(-1, -1) && savedMap[x][y] != RoomType.SPAWN.roomColour -> {
                        tileSize = tileCount
                        calibrateMap(spawnMapPosition)
                        return
                    }
                    spawnMapPosition != Pair(-1, -1) -> {
                        tileCount++
                    }
                    savedMap[x][y] == RoomType.SPAWN.roomColour -> {
                        spawnMapPosition = Pair(x, y)
                        tileCount++
                    }
                }
            }
        }
    }

    private fun calibrateMap(spawnMapPosition: Pair<Int, Int>) {
        mapCalibrated = if (tileSize == 16 || tileSize == 18) {
            scaleFactor = tileSize / DungeonData.ROOM_SIZE.toDouble()

            topLeftTilePos = Pair(
                getMapStartPosition(spawnMapPosition.first),
                getMapStartPosition(spawnMapPosition.second)
            )

            spawnTilePosition = gridPosFromMapPos(spawnMapPosition)
            true
        } else {
            false
        }
    }

    private fun getMapStartPosition(mapCoordinate: Int): Int {
        return if (tileSize == 16) {
            if (mapCoordinate % 2 == 0) 14 else 3
        } else {
            if (mapCoordinate % 2 == 0) 20 else 9
        }
    }

    fun gridPosFromMapPos(mapPosition: Pair<Int, Int>) : Pair<Int, Int> {
        return Pair(
            (mapPosition.first - topLeftTilePos.first) / mapTileSize,
            (mapPosition.second - topLeftTilePos.second) / mapTileSize
        )
    }

    fun mapPosFromGridPos(gridPosition: Pair<Int, Int>) : Pair<Int, Int> {
        return Pair(
            gridPosition.first * mapTileSize + topLeftTilePos.first,
            gridPosition.second * mapTileSize + topLeftTilePos.second
        )
    }
}