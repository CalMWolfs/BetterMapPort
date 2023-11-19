package com.calmwolfs.bettermap.utils

import com.calmwolfs.bettermap.data.getModVector
import net.minecraft.client.Minecraft

object LocationUtils {
    fun playerLocation() = Minecraft.getMinecraft().thePlayer.getModVector()
}