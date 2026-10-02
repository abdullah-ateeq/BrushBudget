package com.example.paintcalculator.util

import com.example.paintcalculator.model.MeasurementUnit
import java.util.Locale

object UnitConverter {
    // Exact conversion factors to meters
    const val MM_TO_METER = 0.001
    const val CM_TO_METER = 0.01
    const val METER_TO_METER = 1.0
    const val INCH_TO_METER = 0.0254
    const val FOOT_TO_METER = 0.3048

    // Area conversion: 1 m² = 10.7639104 ft²
    const val SQ_METER_TO_SQ_FEET = 10.7639104

    fun convertToMeters(value: Double, unit: MeasurementUnit): Double {
        return when (unit) {
            MeasurementUnit.MILLIMETER -> value * MM_TO_METER
            MeasurementUnit.CENTIMETER -> value * CM_TO_METER
            MeasurementUnit.METER -> value * METER_TO_METER
            MeasurementUnit.INCH -> value * INCH_TO_METER
            MeasurementUnit.FOOT -> value * FOOT_TO_METER
        }
    }

    fun convertFromMeters(meters: Double, targetUnit: MeasurementUnit): Double {
        return when (targetUnit) {
            MeasurementUnit.MILLIMETER -> meters / MM_TO_METER
            MeasurementUnit.CENTIMETER -> meters / CM_TO_METER
            MeasurementUnit.METER -> meters / METER_TO_METER
            MeasurementUnit.INCH -> meters / INCH_TO_METER
            MeasurementUnit.FOOT -> meters / FOOT_TO_METER
        }
    }

    fun squareMetersToSquareFeet(m2: Double): Double {
        return m2 * SQ_METER_TO_SQ_FEET
    }

    fun formatDecimal(value: Double, decimals: Int = 2): String {
        if (value.isNaN() || value.isInfinite()) return "0.00"
        return String.format(Locale.US, "%.${decimals}f", value)
    }
}
