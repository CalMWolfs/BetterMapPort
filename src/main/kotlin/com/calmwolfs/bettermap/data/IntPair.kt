package com.calmwolfs.bettermap.data

data class IntPair(val x: Int, val y: Int) {

    override fun toString(): String {
        return "(x: $x, y: $y)"
    }
}