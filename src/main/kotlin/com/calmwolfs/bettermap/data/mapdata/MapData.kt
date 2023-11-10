package com.calmwolfs.bettermap.data.mapdata

enum class RoomState {
    UNOPENED,
    ADJACENT,
    OPENED,
    CLEARED,
    COMPLETED,
    FAILED;
}

enum class RoomType {
    SPAWN,
    NORMAL,
    PUZZLE,
    MINIBOSS,
    FAIRY,
    BLOOD,
    TRAP,
    BLACK,
    UNKNOWN;
}