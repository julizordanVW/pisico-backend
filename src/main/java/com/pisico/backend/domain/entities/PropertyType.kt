package com.pisico.backend.domain.entities

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class PropertyType(val value: String) {
    ROOM("room"),
    APARTMENT("apartment"),
    HOUSE("house"),
    STUDIO("studio"),
    CHALET("chalet"),
    DUPLEX("duplex");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): PropertyType? {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
        }
    }

    @JsonValue
    fun toValue(): String = value
}
