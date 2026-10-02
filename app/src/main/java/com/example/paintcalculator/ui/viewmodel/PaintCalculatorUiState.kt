package com.example.paintcalculator.ui.viewmodel

import com.example.paintcalculator.model.AppSettings
import com.example.paintcalculator.model.CalculationRecord
import com.example.paintcalculator.model.MeasurementUnit
import com.example.paintcalculator.model.PaintCalculationResult
import com.example.paintcalculator.model.PaintOpening

data class PaintCalculatorUiState(
    val wallLengthInput: String = "5.00",
    val wallHeightInput: String = "3.00",
    val measurementUnit: MeasurementUnit = MeasurementUnit.METER,
    val openings: List<PaintOpening> = emptyList(),
    val coats: Int = 2,
    val coverageInput: String = "10.0",
    val wastePercent: Double = 10.0,
    val isCustomWaste: Boolean = false,
    val customWasteInput: String = "",
    val canSizeLiters: Double? = null,
    val isCustomCanSize: Boolean = false,
    val customCanSizeInput: String = "",
    val calculationResult: PaintCalculationResult? = null,
    val wallLengthError: String? = null,
    val wallHeightError: String? = null,
    val coverageError: String? = null,
    val wasteError: String? = null,
    val areaExceedsError: String? = null,
    val canSizeError: String? = null,
    val historyList: List<CalculationRecord> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val userNotification: String? = null
) {
    val isOpeningLimitReached: Boolean
        get() = openings.size >= 10

    val grossWallAreaM2: Double
        get() = calculationResult?.grossWallAreaM2 ?: 0.0

    val openingAreaM2: Double
        get() = calculationResult?.openingAreaM2 ?: 0.0

    val netPaintableAreaM2: Double
        get() = calculationResult?.netPaintableAreaM2 ?: 0.0

    val isInputValid: Boolean
        get() = wallLengthError == null && 
                wallHeightError == null && 
                coverageError == null && 
                wasteError == null && 
                areaExceedsError == null &&
                canSizeError == null &&
                calculationResult != null &&
                (wallLengthInput.toDoubleOrNull() ?: 0.0) > 0 &&
                (wallHeightInput.toDoubleOrNull() ?: 0.0) > 0
}
