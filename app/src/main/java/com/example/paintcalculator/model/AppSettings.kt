package com.example.paintcalculator.model

enum class AppTheme(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromString(value: String): AppTheme {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultUnit: MeasurementUnit = MeasurementUnit.METER,
    val defaultCoats: Int = 2,
    val defaultCoverage: Double = 10.0,
    val defaultWaste: Double = 10.0,
    val defaultCanSize: Double? = null,
    val hapticFeedback: Boolean = true,
    val soundEffects: Boolean = false,
    val showSquareFeet: Boolean = true
)
