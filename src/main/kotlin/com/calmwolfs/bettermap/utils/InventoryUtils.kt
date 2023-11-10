package com.calmwolfs.bettermap.utils

import net.minecraft.client.Minecraft

object InventoryUtils {
    fun getMapSlot() = Minecraft.getMinecraft()?.thePlayer?.inventory?.mainInventory?.get(8)
}