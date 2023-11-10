package com.calmwolfs.bettermap.data.mapdata

enum class RoomState {
    UNOPENED,
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
    WITHER_DOOR(-1),
    UNKNOWN(85);
}