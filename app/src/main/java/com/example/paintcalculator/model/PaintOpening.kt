package com.example.paintcalculator.model

data class PaintOpening(
    val id: Long = System.currentTimeMillis(),
    val type: OpeningType,
    val width: Double,
    val height: Double,
    val quantity: Int = 1,
    val unit: MeasurementUnit = MeasurementUnit.METER
)
