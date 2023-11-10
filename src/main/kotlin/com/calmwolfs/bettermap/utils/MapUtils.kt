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
    private var spawnTileLocation: Pair<Int, Int> = Pair(-1, -1)
    var spawnPosition: Pair<Int, Int> = Pair(-1, -1)
    private var tileSize = 0
    private var mapCalibrated = false

    var scaleFactor = 0.0

    fun isMapCalibrated() = mapCalibrated

    val mapTileSize: Int
        get() = tileSize + 4

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        //todo might want to show the score map so could remove this and just do inDungeon()
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
        if (spawnTileLocation == Pair(-1, -1)) {
            findEntranceCorner()
            calibrateMap()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        clearMap()
    }

    private fun clearMap() {
        mapCalibrated = false
        savedMap = Array(128) { Array(128) { 0 } }
        spawnTileLocation = Pair(-1, -1)
    }

    private fun findEntranceCorner() {
        spawnTileLocation = Pair(-1, -1)
        var tileCount = 0

        for (x in savedMap.indices) {
            for (y in savedMap[x].indices) {
                when {
                    spawnTileLocation != Pair(-1, -1) && savedMap[x][y] != RoomType.SPAWN.roomColour -> {
                        tileSize = tileCount
                        return
                    }
                    spawnTileLocation != Pair(-1, -1) -> {
                        tileCount++
                    }
                    savedMap[x][y] == RoomType.SPAWN.roomColour -> {
                        spawnTileLocation = Pair(x, y)
                        tileCount++
                    }
                }
            }
        }
    }

    private fun calibrateMap() {
        mapCalibrated = if (tileSize == 16 || tileSize == 18) {
            scaleFactor = tileSize / DungeonData.ROOM_SIZE.toDouble()
            spawnPosition = Pair(spawnTileLocation.first / mapTileSize, spawnTileLocation.second / mapTileSize)
            true
        } else {
            false
        }
    }
}