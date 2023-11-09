package com.calmwolfs.bettermap.data.roomdata

import com.calmwolfs.bettermap.data.ModVector
import com.google.gson.annotations.Expose

data class RoomData(
    @Expose val ids: List<String>,
    @Expose val name: String,
    @Expose val type: RoomDataType,
    @Expose val shape: RoomShape,
    @Expose val cryptCount: Int,
    @Expose val secretCount: Int,
    @Expose val hasSpiders: Boolean,
    @Expose val hasFairSoul: Boolean,
    @Expose val secretDetails: RoomSecretData?,
    @Expose val secretLocations: Map<String, List<ModVector>>?
)

data class RoomDataApi(
    @Expose val id: List<String>,
    @Expose val name: String,
    @Expose val type: String,
    @Expose val shape: String,
    @Expose val secrets: Int,
    @Expose val crypts: Int,
    @Expose val spiders: Boolean,
    @Expose val soul: Boolean,
    @Expose val secret_details: RoomSecretData?,
    @Expose val secret_coords: Map<String, List<List<Int>>>?
) {
    fun toRoomData(): RoomData {
        val secretLocations = this.secret_coords?.mapValues { (_, value) ->
            value.map { ModVector(it) }.toMutableList()
        }?.toMutableMap()

        val roomType = RoomDataType.fromApiName(this.type)
        val roomShape = RoomShape.fromApiName(this.shape)

        return RoomData(
            this.id,
            this.name,
            roomType,
            roomShape,
            this.crypts,
            this.secrets,
            this.spiders,
            this.soul,
            this.secret_details,
            secretLocations
        )
    }
}

data class RoomSecretData(
    @Expose val wither: Int,
    @Expose val redstone_key: Int,
    @Expose val bat: Int,
    @Expose val item: Int,
    @Expose val chest: Int
)

enum class RoomShape(val apiName: String) {
    ONE_ONE("1x1"),
    ONE_TWO("1x2"),
    ONE_THREE("1x3"),
    ONE_FOUR("1x4"),
    TWO_TWO("2x2"),
    L_SHAPE("L"),
    UNKNOWN("");

    companion object {
        fun fromApiName(apiName: String): RoomShape {
            return RoomShape.entries.find { it.apiName == apiName } ?: RoomShape.UNKNOWN
        }
    }
}

enum class RoomDataType(val apiName: String) {
    SPAWN("spawn"),
    MOBS("mobs"),
    MINIBOSS("miniboss"),
    RARE("rare"),
    PUZZLE("puzzle"),
    GOLD("gold"),
    FAIRY("fairy"),
    BLOOD("blood"),
    TRAP("trap"),
    UNKNOWN("");

    companion object {
        fun fromApiName(apiName: String): RoomDataType {
            return entries.find { it.apiName == apiName } ?: RoomDataType.UNKNOWN
        }
    }
}
