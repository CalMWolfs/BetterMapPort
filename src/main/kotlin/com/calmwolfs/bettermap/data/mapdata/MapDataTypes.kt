package com.calmwolfs.bettermap.data.mapdata

enum class RoomState {
    ADJACENT,
    OPENED,
    CLEARED,
    COMPLETED,
    FAILED;
}

enum class RoomType(val roomColour: Int) {
    SPAWN(30),
    NORMAL(63),
    PUZZLE(66),
    MINIBOSS(74),
    FAIRY(82),
    BLOOD(18),
    TRAP(62),
    WITHER(119),
    UNKNOWN(0),
    UNOPENED(85);

    companion object {
        fun fromColour(colour: Int): RoomType {
            return RoomType.entries.find { it.roomColour == colour } ?: UNKNOWN
        }
    }
}