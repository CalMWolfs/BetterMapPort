package com.calmwolfs.bettermap.data.mapdata

import com.calmwolfs.bettermap.data.ModPair

data class MapColourArray(private val colours: Array<Array<Int>>) {
    override fun toString(): String {
        val result = StringBuilder()
        for (row in colours) {
            result.append(row.joinToString(",")).append("\n")
        }
        return result.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MapColourArray) return false

        return colours.contentDeepEquals(other.colours)
    }

    operator fun get(x: Int, y: Int): Int {
        return colours[x][y]
    }

    operator fun get(pair: ModPair): Int {
        return colours[pair.first][pair.second]
    }

    fun getColours(): List<List<Int>> {
        return colours.map { it.toList() }
    }

    override fun hashCode(): Int {
        return colours.contentDeepHashCode()
    }

    companion object {
        fun empty() = MapColourArray(Array(128) { Array(128) { 0 } })
    }
}