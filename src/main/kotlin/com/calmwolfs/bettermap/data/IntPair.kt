package com.calmwolfs.bettermap.data

data class IntPair(val x: Int = -1, val y: Int = -1) {

    override fun toString(): String {
        return "(x: $x, y: $y)"
    }
}