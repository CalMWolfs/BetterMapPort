package com.calmwolfs.bettermap.data

data class ModVector(
    val x: Double,
    val y: Double,
    val z: Double,
)  {
    constructor(intList: List<Int>) : this(
        x = intList.getOrNull(0)?.toDouble() ?: -1.0,
        y = intList.getOrNull(1)?.toDouble() ?: -1.0,
        z = intList.getOrNull(2)?.toDouble() ?: -1.0
    )
}
