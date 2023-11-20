package com.calmwolfs.bettermap.utils

import net.minecraft.util.Vec4b

object Vec4bUtils {
    fun Vec4b.mapX() = (this.func_176112_b() + 128) shr 1

    fun Vec4b.mapY() = (this.func_176113_c() + 128) shr 1

    fun Vec4b.yaw() = (this.func_176111_d() * 22.5) + 180
}