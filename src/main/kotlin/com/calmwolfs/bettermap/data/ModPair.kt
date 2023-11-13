package com.calmwolfs.bettermap.data

data class ModPair(val first: Int, val second: Int) {

    override fun toString(): String {
        return "(x: $first, y: $second)"
    }
}