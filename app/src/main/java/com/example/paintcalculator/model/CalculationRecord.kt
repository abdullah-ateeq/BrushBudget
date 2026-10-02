package com.example.paintcalculator.model

data class CalculationRecord(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String,
    val wallLength: Double,
    val wallHeight: Double,
    val unit: MeasurementUnit,
    val grossWallAreaM2: Double,
    val openings: List<PaintOpening>,
    val netPaintableAreaM2: Double,
    val coats: Int,
    val coverageM2PerLiter: Double,
    val wastePercent: Double,
    val basePaintLiters: Double,
    val estimatedPaintLiters: Double,
    val canSizeLiters: Double? = null,
    val requiredCans: Int? = null
)
