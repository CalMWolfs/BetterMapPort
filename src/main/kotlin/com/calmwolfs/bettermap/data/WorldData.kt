package com.calmwolfs.bettermap.data

import com.calmwolfs.bettermap.events.WorldChangeEvent
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object WorldData {
    private val airLocations = mutableListOf<IntPair>()

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        airLocations.clear()
    }

    private fun getBlockAt(x: Int, y: Int, z: Int): Block {
        val theWorld = Minecraft.getMinecraft().theWorld
        return theWorld.getBlockState(BlockPos(x, y, z)).block
    }

    fun getBlockIdAt(x: Int, y: Int, z: Int): Int {
        return Block.getIdFromBlock(getBlockAt(x, y, z))
    }

    fun isBlockAir(x: Int, y: Int, z: Int): Boolean {
        if (IntPair(x, z) in airLocations) return true

        return getBlockIdAt(x, y, z) == 0
    }

    fun addAirLocations(start: IntPair) {
        airLocations.add(IntPair(start.x - 1, start.y - 1))
        airLocations.add(IntPair(start.x, start.y - 1))
        airLocations.add(IntPair(start.x - 1, start.y))

        airLocations.add(IntPair(start.x + 32, start.y - 1))
        airLocations.add(IntPair(start.x + 32 - 1, start.y - 1))
        airLocations.add(IntPair(start.x + 32, start.y - 1))

        airLocations.add(IntPair(start.x - 1, start.y + 32))
        airLocations.add(IntPair(start.x - 1, start.y + 32 - 1))
        airLocations.add(IntPair(start.x, start.y + 32))

        airLocations.add(IntPair(start.x + 32, start.y + 32))
        airLocations.add(IntPair(start.x + 32 - 1, start.y + 32))
        airLocations.add(IntPair(start.x + 32, start.y + 32 - 1))
    }
}