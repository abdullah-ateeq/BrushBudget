package com.example.paintcalculator.model

data class PaintCalculationResult(
    val grossWallAreaM2: Double,
    val openingAreaM2: Double,
    val netPaintableAreaM2: Double,
    val totalCoatAreaM2: Double,
    val basePaintLiters: Double,
    val wasteLiters: Double,
    val requiredPaintLiters: Double,
    val requiredCans: Int? = null,
    val purchasedPaintLiters: Double? = null,
    val extraPaintLiters: Double? = null
) {
    val recommendedPurchaseLiters: Int
        get() = kotlin.math.ceil(requiredPaintLiters).toInt()
}
