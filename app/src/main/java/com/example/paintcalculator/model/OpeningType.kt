package com.example.paintcalculator.model

enum class OpeningType(val displayName: String) {
    DOOR("Door"),
    WINDOW("Window");

    companion object {
        fun fromString(value: String): OpeningType {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.displayName.equals(value, ignoreCase = true) 
            } ?: DOOR
        }
    }
}
