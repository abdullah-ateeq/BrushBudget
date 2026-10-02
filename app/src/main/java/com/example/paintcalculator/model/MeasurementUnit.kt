package com.example.paintcalculator.model

enum class MeasurementUnit(
    val displayName: String,
    val abbreviation: String
) {
    MILLIMETER("Millimeters", "mm"),
    CENTIMETER("Centimeters", "cm"),
    METER("Meters", "m"),
    INCH("Inches", "in"),
    FOOT("Feet", "ft");

    companion object {
        fun fromString(value: String): MeasurementUnit {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.abbreviation.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            } ?: METER
        }
    }
}
