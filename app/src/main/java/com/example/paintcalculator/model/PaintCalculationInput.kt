package com.example.paintcalculator.model

data class PaintCalculationInput(
    val wallLength: Double,
    val wallHeight: Double,
    val unit: MeasurementUnit,
    val openings: List<PaintOpening> = emptyList(),
    val coats: Int = 2,
    val coverageM2PerLiter: Double = 10.0,
    val wastePercent: Double = 10.0,
    val canSizeLiters: Double? = null
)
