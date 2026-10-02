package com.example.paintcalculator

import com.example.paintcalculator.model.MeasurementUnit
import com.example.paintcalculator.model.OpeningType
import com.example.paintcalculator.model.PaintCalculationInput
import com.example.paintcalculator.model.PaintOpening
import com.example.paintcalculator.util.PaintCalculatorEngine
import com.example.paintcalculator.util.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaintCalculatorUnitTest {

    private val delta = 0.0001

    // 1. Meter conversion
    @Test
    fun test1_meterConversion() {
        val meters = UnitConverter.convertToMeters(5.0, MeasurementUnit.METER)
        assertEquals(5.0, meters, delta)
    }

    // 2. Centimeter conversion
    @Test
    fun test2_centimeterConversion() {
        val meters = UnitConverter.convertToMeters(500.0, MeasurementUnit.CENTIMETER)
        assertEquals(5.0, meters, delta)
    }

    // 3. Millimeter conversion
    @Test
    fun test3_millimeterConversion() {
        val meters = UnitConverter.convertToMeters(5000.0, MeasurementUnit.MILLIMETER)
        assertEquals(5.0, meters, delta)
    }

    // 4. Inch conversion
    @Test
    fun test4_inchConversion() {
        val meters = UnitConverter.convertToMeters(100.0, MeasurementUnit.INCH)
        assertEquals(2.54, meters, delta)
    }

    // 5. Foot conversion
    @Test
    fun test5_footConversion() {
        val meters = UnitConverter.convertToMeters(10.0, MeasurementUnit.FOOT)
        assertEquals(3.048, meters, delta)
    }

    // 6. Gross wall area
    @Test
    fun test6_grossWallArea() {
        val area = PaintCalculatorEngine.calculateGrossWallArea(5.0, 3.0, MeasurementUnit.METER)
        assertEquals(15.0, area, delta)
    }

    // 7. Door deduction
    @Test
    fun test7_doorDeduction() {
        val door = PaintOpening(
            id = 1L,
            type = OpeningType.DOOR,
            width = 0.9,
            height = 2.1,
            quantity = 1,
            unit = MeasurementUnit.METER
        )
        val grossArea = PaintCalculatorEngine.calculateGrossWallArea(5.0, 3.0, MeasurementUnit.METER)
        val doorArea = PaintCalculatorEngine.calculateOpeningArea(door)
        val netArea = PaintCalculatorEngine.calculateNetPaintableArea(grossArea, doorArea)

        assertEquals(1.89, doorArea, delta)
        assertEquals(13.11, netArea, delta)
    }

    // 8. Window deduction
    @Test
    fun test8_windowDeduction() {
        val window = PaintOpening(
            id = 2L,
            type = OpeningType.WINDOW,
            width = 1.5,
            height = 1.2,
            quantity = 1,
            unit = MeasurementUnit.METER
        )
        val grossArea = PaintCalculatorEngine.calculateGrossWallArea(5.0, 3.0, MeasurementUnit.METER)
        val windowArea = PaintCalculatorEngine.calculateOpeningArea(window)
        val netArea = PaintCalculatorEngine.calculateNetPaintableArea(grossArea, windowArea)

        assertEquals(1.80, windowArea, delta)
        assertEquals(13.20, netArea, delta)
    }

    // 9. Multiple opening quantities
    @Test
    fun test9_multipleOpeningQuantities() {
        val door = PaintOpening(
            id = 1L,
            type = OpeningType.DOOR,
            width = 0.9,
            height = 2.1,
            quantity = 2,
            unit = MeasurementUnit.METER
        )
        val window = PaintOpening(
            id = 2L,
            type = OpeningType.WINDOW,
            width = 1.5,
            height = 1.2,
            quantity = 2,
            unit = MeasurementUnit.METER
        )
        val totalOpenings = PaintCalculatorEngine.calculateTotalOpeningArea(listOf(door, window))

        // Door: 0.9 * 2.1 * 2 = 3.78
        // Window: 1.5 * 1.2 * 2 = 3.60
        // Total = 7.38
        assertEquals(7.38, totalOpenings, delta)
    }

    // 10. Net paintable area
    @Test
    fun test10_netPaintableArea() {
        val grossArea = 15.0
        val openingArea = 3.69
        val netArea = PaintCalculatorEngine.calculateNetPaintableArea(grossArea, openingArea)
        assertEquals(11.31, netArea, delta)

        // Opening area exceeding gross area must never be negative
        val negativeAreaCase = PaintCalculatorEngine.calculateNetPaintableArea(10.0, 15.0)
        assertEquals(0.0, negativeAreaCase, delta)
    }

    // 11. Multiple coats
    @Test
    fun test11_multipleCoats() {
        val netArea = 11.31
        val coatArea2 = PaintCalculatorEngine.calculateTotalCoatArea(netArea, 2)
        val coatArea3 = PaintCalculatorEngine.calculateTotalCoatArea(netArea, 3)

        assertEquals(22.62, coatArea2, delta)
        assertEquals(33.93, coatArea3, delta)
    }

    // 12. Coverage calculation
    @Test
    fun test12_coverageCalculation() {
        val coatArea = 22.62
        val coverage = 10.0
        val basePaint = PaintCalculatorEngine.calculateBasePaint(coatArea, coverage)
        assertEquals(2.262, basePaint, delta)
    }

    // 13. Waste calculation
    @Test
    fun test13_wasteCalculation() {
        val basePaint = 2.262
        val wastePercent = 10.0
        val wasteLiters = PaintCalculatorEngine.calculateWasteLiters(basePaint, wastePercent)
        val requiredPaint = PaintCalculatorEngine.calculateRequiredPaint(basePaint, wastePercent)

        assertEquals(0.2262, wasteLiters, delta)
        assertEquals(2.4882, requiredPaint, delta)
    }

    // 14. Can calculation
    @Test
    fun test14_canCalculation() {
        val requiredPaint = 2.4882
        val canSize = 2.5
        val (canCount, purchased, extra) = PaintCalculatorEngine.calculateCanEstimation(requiredPaint, canSize)

        assertEquals(1, canCount)
        assertEquals(2.5, purchased!!, delta)
        assertEquals(0.0118, extra!!, delta)

        // 1.0 L can size
        val (canCount1L, purchased1L, extra1L) = PaintCalculatorEngine.calculateCanEstimation(requiredPaint, 1.0)
        assertEquals(3, canCount1L)
        assertEquals(3.0, purchased1L!!, delta)
        assertEquals(0.5118, extra1L!!, delta)
    }

    // 15. Extra paint calculation
    @Test
    fun test15_extraPaintCalculation() {
        val requiredPaint = 8.80
        val canSize = 5.0
        val (canCount, purchased, extra) = PaintCalculatorEngine.calculateCanEstimation(requiredPaint, canSize)

        // 2 cans of 5.0L = 10.0L
        assertEquals(2, canCount)
        assertEquals(10.0, purchased!!, delta)
        assertEquals(1.20, extra!!, delta)
    }

    // 16. Zero validation
    @Test
    fun test16_zeroValidation() {
        assertNotNull(PaintCalculatorEngine.validateWallLength(0.0))
        assertNotNull(PaintCalculatorEngine.validateWallHeight(0.0))
        assertNull(PaintCalculatorEngine.validateWallLength(5.0))
        assertNull(PaintCalculatorEngine.validateWallHeight(3.0))
    }

    // 17. Negative validation
    @Test
    fun test17_negativeValidation() {
        assertNotNull(PaintCalculatorEngine.validateWallLength(-1.0))
        assertNotNull(PaintCalculatorEngine.validateWallHeight(-3.5))
    }

    // 18. Coverage validation
    @Test
    fun test18_coverageValidation() {
        assertNotNull(PaintCalculatorEngine.validateCoverage(0.0))
        assertNotNull(PaintCalculatorEngine.validateCoverage(-5.0))
        assertNotNull(PaintCalculatorEngine.validateCoverage(null))
        assertNull(PaintCalculatorEngine.validateCoverage(10.0))
    }

    // 19. Waste validation
    @Test
    fun test19_wasteValidation() {
        assertNotNull(PaintCalculatorEngine.validateWaste(-1.0))
        assertNotNull(PaintCalculatorEngine.validateWaste(101.0))
        assertNotNull(PaintCalculatorEngine.validateWaste(null))
        assertNull(PaintCalculatorEngine.validateWaste(0.0))
        assertNull(PaintCalculatorEngine.validateWaste(10.0))
        assertNull(PaintCalculatorEngine.validateWaste(100.0))
    }

    // 20. Opening validation
    @Test
    fun test20_openingValidation() {
        assertNotNull(PaintCalculatorEngine.validateOpening(0.0, 2.0, 1))
        assertNotNull(PaintCalculatorEngine.validateOpening(1.0, 0.0, 1))
        assertNotNull(PaintCalculatorEngine.validateOpening(1.0, 2.0, 0))
        assertNotNull(PaintCalculatorEngine.validateOpening(-1.0, 2.0, 1))
        assertNull(PaintCalculatorEngine.validateOpening(0.9, 2.1, 1))
    }

    // Specification Example End-to-End Test
    @Test
    fun test_specificationExample() {
        val input = PaintCalculationInput(
            wallLength = 5.0,
            wallHeight = 3.0,
            unit = MeasurementUnit.METER,
            openings = listOf(
                PaintOpening(1L, OpeningType.DOOR, 0.9, 2.1, 1, MeasurementUnit.METER),
                PaintOpening(2L, OpeningType.WINDOW, 1.5, 1.2, 1, MeasurementUnit.METER)
            ),
            coats = 2,
            coverageM2PerLiter = 10.0,
            wastePercent = 10.0
        )

        val result = PaintCalculatorEngine.calculatePaint(input)

        assertEquals(15.0, result.grossWallAreaM2, delta)
        assertEquals(3.69, result.openingAreaM2, delta)
        assertEquals(11.31, result.netPaintableAreaM2, delta)
        assertEquals(22.62, result.totalCoatAreaM2, delta)
        assertEquals(2.262, result.basePaintLiters, delta)
        assertEquals(0.2262, result.wasteLiters, delta)
        assertEquals(2.4882, result.requiredPaintLiters, delta)
        assertEquals("2.49", UnitConverter.formatDecimal(result.requiredPaintLiters))
        assertEquals(3, result.recommendedPurchaseLiters)
    }

    // Area conversion test (m² to ft²)
    @Test
    fun test_areaConversion() {
        val sqFeet = UnitConverter.squareMetersToSquareFeet(1.0)
        assertEquals(10.7639104, sqFeet, delta)
    }
}
