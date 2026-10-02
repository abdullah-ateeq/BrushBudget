package com.example.paintcalculator.util

import com.example.paintcalculator.model.MeasurementUnit
import com.example.paintcalculator.model.PaintCalculationInput
import com.example.paintcalculator.model.PaintCalculationResult
import com.example.paintcalculator.model.PaintOpening
import kotlin.math.ceil

object PaintCalculatorEngine {

    const val MAX_OPENINGS = 10
    const val MIN_COATS = 1
    const val MAX_COATS = 5

    fun calculateGrossWallArea(length: Double, height: Double, unit: MeasurementUnit): Double {
        if (length <= 0 || height <= 0) return 0.0
        val lengthMeters = UnitConverter.convertToMeters(length, unit)
        val heightMeters = UnitConverter.convertToMeters(height, unit)
        return lengthMeters * heightMeters
    }

    fun calculateOpeningArea(opening: PaintOpening): Double {
        if (opening.width <= 0 || opening.height <= 0 || opening.quantity < 1) return 0.0
        val widthMeters = UnitConverter.convertToMeters(opening.width, opening.unit)
        val heightMeters = UnitConverter.convertToMeters(opening.height, opening.unit)
        return widthMeters * heightMeters * opening.quantity
    }

    fun calculateTotalOpeningArea(openings: List<PaintOpening>): Double {
        return openings.sumOf { calculateOpeningArea(it) }
    }

    fun calculateNetPaintableArea(grossWallAreaM2: Double, totalOpeningAreaM2: Double): Double {
        return maxOf(0.0, grossWallAreaM2 - totalOpeningAreaM2)
    }

    fun calculateTotalCoatArea(netPaintableAreaM2: Double, coats: Int): Double {
        if (netPaintableAreaM2 <= 0 || coats < MIN_COATS) return 0.0
        return netPaintableAreaM2 * coats
    }

    fun calculateBasePaint(totalCoatAreaM2: Double, coverageM2PerLiter: Double): Double {
        if (totalCoatAreaM2 <= 0 || coverageM2PerLiter <= 0) return 0.0
        return totalCoatAreaM2 / coverageM2PerLiter
    }

    fun calculateWasteLiters(basePaintLiters: Double, wastePercent: Double): Double {
        if (basePaintLiters <= 0 || wastePercent <= 0) return 0.0
        return basePaintLiters * (wastePercent / 100.0)
    }

    fun calculateRequiredPaint(basePaintLiters: Double, wastePercent: Double): Double {
        if (basePaintLiters <= 0) return 0.0
        val clampedWaste = maxOf(0.0, wastePercent)
        return basePaintLiters * (1.0 + clampedWaste / 100.0)
    }

    fun calculateCanEstimation(
        requiredPaintLiters: Double,
        canSizeLiters: Double?
    ): Triple<Int?, Double?, Double?> {
        if (canSizeLiters == null || canSizeLiters <= 0 || requiredPaintLiters <= 0) {
            return Triple(null, null, null)
        }
        val canCount = ceil(requiredPaintLiters / canSizeLiters).toInt()
        val totalPurchased = canCount * canSizeLiters
        val extraPaint = maxOf(0.0, totalPurchased - requiredPaintLiters)
        return Triple(canCount, totalPurchased, extraPaint)
    }

    fun calculatePaint(input: PaintCalculationInput): PaintCalculationResult {
        val grossWallAreaM2 = calculateGrossWallArea(input.wallLength, input.wallHeight, input.unit)
        val openingAreaM2 = calculateTotalOpeningArea(input.openings)
        val netPaintableAreaM2 = calculateNetPaintableArea(grossWallAreaM2, openingAreaM2)
        val totalCoatAreaM2 = calculateTotalCoatArea(netPaintableAreaM2, input.coats)
        val basePaintLiters = calculateBasePaint(totalCoatAreaM2, input.coverageM2PerLiter)
        val wasteLiters = calculateWasteLiters(basePaintLiters, input.wastePercent)
        val requiredPaintLiters = calculateRequiredPaint(basePaintLiters, input.wastePercent)

        val (requiredCans, purchasedPaintLiters, extraPaintLiters) = calculateCanEstimation(
            requiredPaintLiters,
            input.canSizeLiters
        )

        return PaintCalculationResult(
            grossWallAreaM2 = grossWallAreaM2,
            openingAreaM2 = openingAreaM2,
            netPaintableAreaM2 = netPaintableAreaM2,
            totalCoatAreaM2 = totalCoatAreaM2,
            basePaintLiters = basePaintLiters,
            wasteLiters = wasteLiters,
            requiredPaintLiters = requiredPaintLiters,
            requiredCans = requiredCans,
            purchasedPaintLiters = purchasedPaintLiters,
            extraPaintLiters = extraPaintLiters
        )
    }

    // Validation functions
    fun validateWallLength(length: Double?): String? {
        if (length == null) return "Wall length is required."
        if (length <= 0) return "Wall length must be greater than zero."
        return null
    }

    fun validateWallHeight(height: Double?): String? {
        if (height == null) return "Wall height is required."
        if (height <= 0) return "Wall height must be greater than zero."
        return null
    }

    fun validateCoats(coats: Int): String? {
        if (coats < MIN_COATS || coats > MAX_COATS) {
            return "Number of coats must be between $MIN_COATS and $MAX_COATS."
        }
        return null
    }

    fun validateCoverage(coverage: Double?): String? {
        if (coverage == null) return "Coverage is required."
        if (coverage <= 0) return "Coverage must be greater than zero."
        return null
    }

    fun validateWaste(waste: Double?): String? {
        if (waste == null) return "Waste is required."
        if (waste < 0 || waste > 100) return "Waste must be between 0% and 100%."
        return null
    }

    fun validateOpening(width: Double?, height: Double?, quantity: Int?): String? {
        if (width == null || width <= 0) return "Opening width must be greater than zero."
        if (height == null || height <= 0) return "Opening height must be greater than zero."
        if (quantity == null || quantity < 1) return "Quantity must be at least 1."
        return null
    }

    fun validateCanSize(canSize: Double?): String? {
        if (canSize != null && canSize <= 0) return "Can size must be greater than zero."
        return null
    }

    fun validateAreaExceeds(grossArea: Double, openingArea: Double): String? {
        if (grossArea > 0 && openingArea > grossArea) {
            return "Opening area cannot exceed wall area."
        }
        return null
    }
}
