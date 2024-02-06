package com.calmwolfs.bettermap.data

import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos

object WorldData {
    private fun getBlockAt(x: Int, y: Int, z: Int): Block {
        val theWorld = Minecraft.getMinecraft().theWorld
        return theWorld.getBlockState(BlockPos(x, y, z)).block
    }

    fun getBlockIdAt(x: Int, y: Int, z: Int): Int {
        return Block.getIdFromBlock(getBlockAt(x, y, z))
    }

    fun isBlockAir(x: Int, y: Int, z: Int): Boolean {
        // todo spawn air blocks when setting reset old ones

        return getBlockIdAt(x, y, z) == 0
    }
}