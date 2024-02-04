package com.calmwolfs.bettermap.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject

object JsonUtils {
    fun JsonObject.getStringOrValue(key: String, alternative: String): String {
        return if (has(key) && get(key).isJsonPrimitive) {
            get(key).asString
        } else {
            alternative
        }
    }

    fun JsonObject.getStringOrNull(key: String): String? {
        return if (has(key) && get(key).isJsonPrimitive) {
            get(key).asString
        } else {
            null
        }
    }

    fun JsonObject.getIntOrValue(key: String, alternative: Int = -1): Int {
        return if (has(key) && get(key).isJsonPrimitive) {
            get(key).asInt
        } else {
            alternative
        }
    }

    fun JsonElement.asBooleanOrFalse(): Boolean {
        return try {
            this.asBoolean
        } catch (_: Exception) {
            false
        }
    }
}