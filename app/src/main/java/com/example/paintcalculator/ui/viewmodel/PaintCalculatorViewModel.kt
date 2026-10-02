package com.example.paintcalculator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintcalculator.data.PreferencesManager
import com.example.paintcalculator.model.AppSettings
import com.example.paintcalculator.model.CalculationRecord
import com.example.paintcalculator.model.MeasurementUnit
import com.example.paintcalculator.model.PaintCalculationInput
import com.example.paintcalculator.model.PaintCalculationResult
import com.example.paintcalculator.model.PaintOpening
import com.example.paintcalculator.util.PaintCalculatorEngine
import com.example.paintcalculator.util.SoundAndHapticHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaintCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val soundAndHapticHelper = SoundAndHapticHelper(application)

    private val _uiState = MutableStateFlow(PaintCalculatorUiState())
    val uiState: StateFlow<PaintCalculatorUiState> = _uiState.asStateFlow()

    init {
        // Collect settings
        viewModelScope.launch {
            preferencesManager.settingsFlow.collectLatest { settings ->
                _uiState.update { current ->
                    current.copy(appSettings = settings)
                }
            }
        }

        // Collect history
        viewModelScope.launch {
            preferencesManager.historyFlow.collectLatest { history ->
                _uiState.update { current ->
                    current.copy(historyList = history)
                }
            }
        }

        // Initial live calculation with defaults
        recompute()
    }

    override fun onCleared() {
        super.onCleared()
        soundAndHapticHelper.release()
    }

    fun onWallLengthChanged(value: String) {
        _uiState.update { it.copy(wallLengthInput = value) }
        recompute()
    }

    fun onWallHeightChanged(value: String) {
        _uiState.update { it.copy(wallHeightInput = value) }
        recompute()
    }

    fun onUnitChanged(unit: MeasurementUnit) {
        _uiState.update { it.copy(measurementUnit = unit) }
        recompute()
    }

    fun onCoatsChanged(coats: Int) {
        if (coats in PaintCalculatorEngine.MIN_COATS..PaintCalculatorEngine.MAX_COATS) {
            _uiState.update { it.copy(coats = coats) }
            recompute()
        }
    }

    fun onCoverageChanged(value: String) {
        _uiState.update { it.copy(coverageInput = value) }
        recompute()
    }

    fun onCoveragePresetSelected(coverage: Double) {
        _uiState.update { it.copy(coverageInput = coverage.toString()) }
        recompute()
    }

    fun onWastePresetSelected(wastePercent: Double) {
        _uiState.update {
            it.copy(
                wastePercent = wastePercent,
                isCustomWaste = false,
                customWasteInput = ""
            )
        }
        recompute()
    }

    fun onCustomWasteToggled() {
        _uiState.update {
            it.copy(
                isCustomWaste = true,
                customWasteInput = it.wastePercent.toString()
            )
        }
        recompute()
    }

    fun onCustomWasteChanged(value: String) {
        val parsed = value.toDoubleOrNull()
        _uiState.update {
            it.copy(
                customWasteInput = value,
                wastePercent = parsed ?: it.wastePercent
            )
        }
        recompute()
    }

    fun onCanSizeSelected(canSize: Double?) {
        _uiState.update {
            it.copy(
                canSizeLiters = canSize,
                isCustomCanSize = false,
                customCanSizeInput = ""
            )
        }
        recompute()
    }

    fun onCustomCanSizeToggled() {
        _uiState.update {
            it.copy(
                isCustomCanSize = true,
                customCanSizeInput = it.canSizeLiters?.toString() ?: ""
            )
        }
        recompute()
    }

    fun onCustomCanSizeChanged(value: String) {
        val parsed = value.toDoubleOrNull()
        _uiState.update {
            it.copy(
                customCanSizeInput = value,
                canSizeLiters = parsed
            )
        }
        recompute()
    }

    fun addOpening(opening: PaintOpening) {
        val current = _uiState.value
        if (current.openings.size >= PaintCalculatorEngine.MAX_OPENINGS) {
            _uiState.update { it.copy(userNotification = "Maximum 10 openings reached.") }
            return
        }
        val updatedOpenings = current.openings + opening
        _uiState.update { it.copy(openings = updatedOpenings) }
        recompute()
        triggerLightFeedback()
    }

    fun removeOpening(openingId: Long) {
        val updatedOpenings = _uiState.value.openings.filter { it.id != openingId }
        _uiState.update { it.copy(openings = updatedOpenings) }
        recompute()
        triggerLightFeedback()
    }

    fun calculateExplicit() {
        recompute()
        val settings = _uiState.value.appSettings
        soundAndHapticHelper.triggerActionFeedback(
            hapticsEnabled = settings.hapticFeedback,
            soundEnabled = settings.soundEffects
        )
    }

    fun resetToDefaults() {
        val settings = _uiState.value.appSettings
        _uiState.update {
            it.copy(
                wallLengthInput = "",
                wallHeightInput = "",
                measurementUnit = settings.defaultUnit,
                openings = emptyList(),
                coats = settings.defaultCoats,
                coverageInput = settings.defaultCoverage.toString(),
                wastePercent = settings.defaultWaste,
                isCustomWaste = false,
                customWasteInput = "",
                canSizeLiters = settings.defaultCanSize,
                isCustomCanSize = false,
                customCanSizeInput = "",
                calculationResult = null,
                wallLengthError = null,
                wallHeightError = null,
                coverageError = null,
                wasteError = null,
                areaExceedsError = null,
                canSizeError = null,
                userNotification = "Calculator reset to defaults."
            )
        }
        recompute()
        triggerLightFeedback()
    }

    fun saveCurrentCalculation() {
        val state = _uiState.value
        val result = state.calculationResult

        if (!state.isInputValid || result == null) {
            _uiState.update { it.copy(userNotification = "Cannot save: Please fix validation errors.") }
            return
        }

        val length = state.wallLengthInput.toDoubleOrNull() ?: 0.0
        val height = state.wallHeightInput.toDoubleOrNull() ?: 0.0
        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val record = CalculationRecord(
            id = System.currentTimeMillis(),
            timestamp = System.currentTimeMillis(),
            formattedDate = dateStr,
            wallLength = length,
            wallHeight = height,
            unit = state.measurementUnit,
            grossWallAreaM2 = result.grossWallAreaM2,
            openings = state.openings,
            netPaintableAreaM2 = result.netPaintableAreaM2,
            coats = state.coats,
            coverageM2PerLiter = state.coverageInput.toDoubleOrNull() ?: 10.0,
            wastePercent = state.wastePercent,
            basePaintLiters = result.basePaintLiters,
            estimatedPaintLiters = result.requiredPaintLiters,
            canSizeLiters = state.canSizeLiters,
            requiredCans = result.requiredCans
        )

        viewModelScope.launch {
            preferencesManager.saveCalculation(record)
            _uiState.update { it.copy(userNotification = "Calculation saved to History.") }
            val settings = _uiState.value.appSettings
            soundAndHapticHelper.triggerActionFeedback(
                hapticsEnabled = settings.hapticFeedback,
                soundEnabled = settings.soundEffects
            )
        }
    }

    fun restoreCalculation(record: CalculationRecord) {
        val isPresetWaste = record.wastePercent in listOf(5.0, 10.0, 15.0, 20.0)
        val isPresetCan = record.canSizeLiters == null || record.canSizeLiters in listOf(1.0, 2.5, 5.0, 10.0, 15.0, 20.0)

        _uiState.update {
            it.copy(
                wallLengthInput = record.wallLength.toString(),
                wallHeightInput = record.wallHeight.toString(),
                measurementUnit = record.unit,
                openings = record.openings,
                coats = record.coats,
                coverageInput = record.coverageM2PerLiter.toString(),
                wastePercent = record.wastePercent,
                isCustomWaste = !isPresetWaste,
                customWasteInput = if (!isPresetWaste) record.wastePercent.toString() else "",
                canSizeLiters = record.canSizeLiters,
                isCustomCanSize = !isPresetCan,
                customCanSizeInput = if (!isPresetCan) record.canSizeLiters?.toString() ?: "" else "",
                userNotification = "Calculation restored from History."
            )
        }
        recompute()
        triggerLightFeedback()
    }

    fun deleteHistoryRecord(id: Long) {
        viewModelScope.launch {
            preferencesManager.deleteCalculation(id)
            triggerLightFeedback()
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            preferencesManager.clearHistory()
            _uiState.update { it.copy(userNotification = "History cleared.") }
            triggerLightFeedback()
        }
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            preferencesManager.updateSettings(settings)
            _uiState.update { it.copy(userNotification = "Settings updated.") }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            preferencesManager.resetSettings()
            _uiState.update { it.copy(userNotification = "Settings reset to defaults.") }
        }
    }

    fun dismissNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }

    private fun triggerLightFeedback() {
        soundAndHapticHelper.triggerHaptic(enabled = _uiState.value.appSettings.hapticFeedback)
    }

    private fun recompute() {
        val state = _uiState.value
        val length = state.wallLengthInput.toDoubleOrNull()
        val height = state.wallHeightInput.toDoubleOrNull()
        val coverage = state.coverageInput.toDoubleOrNull()
        val waste = state.wastePercent
        val canSize = state.canSizeLiters

        // Validations
        val lengthError = PaintCalculatorEngine.validateWallLength(length)
        val heightError = PaintCalculatorEngine.validateWallHeight(height)
        val coverageError = PaintCalculatorEngine.validateCoverage(coverage)
        val wasteError = PaintCalculatorEngine.validateWaste(waste)
        val canSizeError = PaintCalculatorEngine.validateCanSize(canSize)

        var grossArea = 0.0
        var totalOpeningArea = 0.0
        var areaExceedsError: String? = null

        if (length != null && height != null && length > 0 && height > 0) {
            grossArea = PaintCalculatorEngine.calculateGrossWallArea(length, height, state.measurementUnit)
            totalOpeningArea = PaintCalculatorEngine.calculateTotalOpeningArea(state.openings)
            areaExceedsError = PaintCalculatorEngine.validateAreaExceeds(grossArea, totalOpeningArea)
        }

        val hasBlockingError = lengthError != null || 
                               heightError != null || 
                               coverageError != null || 
                               wasteError != null || 
                               canSizeError != null || 
                               areaExceedsError != null

        val result = if (!hasBlockingError && length != null && height != null && coverage != null) {
            val input = PaintCalculationInput(
                wallLength = length,
                wallHeight = height,
                unit = state.measurementUnit,
                openings = state.openings,
                coats = state.coats,
                coverageM2PerLiter = coverage,
                wastePercent = waste,
                canSizeLiters = canSize
            )
            PaintCalculatorEngine.calculatePaint(input)
        } else {
            // Provide partial areas for live display if length and height are valid
            if (grossArea > 0) {
                val netArea = PaintCalculatorEngine.calculateNetPaintableArea(grossArea, totalOpeningArea)
                PaintCalculationResult(
                    grossWallAreaM2 = grossArea,
                    openingAreaM2 = totalOpeningArea,
                    netPaintableAreaM2 = netArea,
                    totalCoatAreaM2 = 0.0,
                    basePaintLiters = 0.0,
                    wasteLiters = 0.0,
                    requiredPaintLiters = 0.0
                )
            } else null
        }

        _uiState.update {
            it.copy(
                wallLengthError = lengthError,
                wallHeightError = heightError,
                coverageError = coverageError,
                wasteError = wasteError,
                canSizeError = canSizeError,
                areaExceedsError = areaExceedsError,
                calculationResult = result
            )
        }
    }
}
