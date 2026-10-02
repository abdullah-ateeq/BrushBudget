package com.example.paintcalculator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.paintcalculator.model.AppSettings
import com.example.paintcalculator.model.AppTheme
import com.example.paintcalculator.model.CalculationRecord
import com.example.paintcalculator.model.MeasurementUnit
import com.example.paintcalculator.model.OpeningType
import com.example.paintcalculator.model.PaintOpening
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "paint_calculator_preferences")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("app_theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_UNIT = stringPreferencesKey("default_unit")
        val DEFAULT_COATS = intPreferencesKey("default_coats")
        val DEFAULT_COVERAGE = doublePreferencesKey("default_coverage")
        val DEFAULT_WASTE = doublePreferencesKey("default_waste")
        val DEFAULT_CAN_SIZE = stringPreferencesKey("default_can_size")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        val SHOW_SQUARE_FEET = booleanPreferencesKey("show_square_feet")
        val HISTORY_JSON = stringPreferencesKey("history_json")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeStr = preferences[PreferencesKeys.THEME] ?: AppTheme.SYSTEM.name
            val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
            val unitStr = preferences[PreferencesKeys.DEFAULT_UNIT] ?: MeasurementUnit.METER.name
            val defaultCoats = preferences[PreferencesKeys.DEFAULT_COATS] ?: 2
            val defaultCoverage = preferences[PreferencesKeys.DEFAULT_COVERAGE] ?: 10.0
            val defaultWaste = preferences[PreferencesKeys.DEFAULT_WASTE] ?: 10.0
            val defaultCanSizeStr = preferences[PreferencesKeys.DEFAULT_CAN_SIZE]
            val haptic = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
            val sound = preferences[PreferencesKeys.SOUND_EFFECTS] ?: false
            val showSqFt = preferences[PreferencesKeys.SHOW_SQUARE_FEET] ?: true

            val canSize = defaultCanSizeStr?.toDoubleOrNull()

            AppSettings(
                theme = AppTheme.fromString(themeStr),
                dynamicColor = dynamicColor,
                defaultUnit = MeasurementUnit.fromString(unitStr),
                defaultCoats = defaultCoats,
                defaultCoverage = defaultCoverage,
                defaultWaste = defaultWaste,
                defaultCanSize = canSize,
                hapticFeedback = haptic,
                soundEffects = sound,
                showSquareFeet = showSqFt
            )
        }

    val historyFlow: Flow<List<CalculationRecord>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[PreferencesKeys.HISTORY_JSON] ?: "[]"
            parseHistoryJson(jsonString)
        }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = settings.theme.name
            preferences[PreferencesKeys.DYNAMIC_COLOR] = settings.dynamicColor
            preferences[PreferencesKeys.DEFAULT_UNIT] = settings.defaultUnit.name
            preferences[PreferencesKeys.DEFAULT_COATS] = settings.defaultCoats
            preferences[PreferencesKeys.DEFAULT_COVERAGE] = settings.defaultCoverage
            preferences[PreferencesKeys.DEFAULT_WASTE] = settings.defaultWaste
            if (settings.defaultCanSize != null) {
                preferences[PreferencesKeys.DEFAULT_CAN_SIZE] = settings.defaultCanSize.toString()
            } else {
                preferences.remove(PreferencesKeys.DEFAULT_CAN_SIZE)
            }
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = settings.hapticFeedback
            preferences[PreferencesKeys.SOUND_EFFECTS] = settings.soundEffects
            preferences[PreferencesKeys.SHOW_SQUARE_FEET] = settings.showSquareFeet
        }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = AppTheme.SYSTEM.name
            preferences[PreferencesKeys.DYNAMIC_COLOR] = true
            preferences[PreferencesKeys.DEFAULT_UNIT] = MeasurementUnit.METER.name
            preferences[PreferencesKeys.DEFAULT_COATS] = 2
            preferences[PreferencesKeys.DEFAULT_COVERAGE] = 10.0
            preferences[PreferencesKeys.DEFAULT_WASTE] = 10.0
            preferences.remove(PreferencesKeys.DEFAULT_CAN_SIZE)
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = true
            preferences[PreferencesKeys.SOUND_EFFECTS] = false
            preferences[PreferencesKeys.SHOW_SQUARE_FEET] = true
        }
    }

    suspend fun saveCalculation(record: CalculationRecord) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[PreferencesKeys.HISTORY_JSON] ?: "[]"
            val currentList = parseHistoryJson(currentJson).toMutableList()

            // Prepend new record
            currentList.add(0, record)

            // Limit to max 50 records (remove oldest)
            while (currentList.size > 50) {
                currentList.removeAt(currentList.lastIndex)
            }

            preferences[PreferencesKeys.HISTORY_JSON] = serializeHistoryJson(currentList)
        }
    }

    suspend fun deleteCalculation(id: Long) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[PreferencesKeys.HISTORY_JSON] ?: "[]"
            val currentList = parseHistoryJson(currentJson).filter { it.id != id }
            preferences[PreferencesKeys.HISTORY_JSON] = serializeHistoryJson(currentList)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HISTORY_JSON] = "[]"
        }
    }

    private fun parseHistoryJson(jsonString: String): List<CalculationRecord> {
        val list = mutableListOf<CalculationRecord>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optLong("id", System.currentTimeMillis())
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                val formattedDate = obj.optString("formattedDate", "")
                val wallLength = obj.optDouble("wallLength", 0.0)
                val wallHeight = obj.optDouble("wallHeight", 0.0)
                val unitStr = obj.optString("unit", MeasurementUnit.METER.name)
                val grossWallAreaM2 = obj.optDouble("grossWallAreaM2", 0.0)
                val netPaintableAreaM2 = obj.optDouble("netPaintableAreaM2", 0.0)
                val coats = obj.optInt("coats", 2)
                val coverage = obj.optDouble("coverageM2PerLiter", 10.0)
                val waste = obj.optDouble("wastePercent", 10.0)
                val basePaint = obj.optDouble("basePaintLiters", 0.0)
                val estimatedPaint = obj.optDouble("estimatedPaintLiters", 0.0)
                val canSize = if (obj.has("canSizeLiters") && !obj.isNull("canSizeLiters")) {
                    obj.getDouble("canSizeLiters")
                } else null
                val requiredCans = if (obj.has("requiredCans") && !obj.isNull("requiredCans")) {
                    obj.getInt("requiredCans")
                } else null

                val openingsList = mutableListOf<PaintOpening>()
                val openingsArray = obj.optJSONArray("openings")
                if (openingsArray != null) {
                    for (j in 0 until openingsArray.length()) {
                        val openObj = openingsArray.getJSONObject(j)
                        val openId = openObj.optLong("id", System.currentTimeMillis())
                        val openTypeStr = openObj.optString("type", OpeningType.DOOR.name)
                        val openWidth = openObj.optDouble("width", 0.0)
                        val openHeight = openObj.optDouble("height", 0.0)
                        val openQty = openObj.optInt("quantity", 1)
                        val openUnitStr = openObj.optString("unit", MeasurementUnit.METER.name)
                        openingsList.add(
                            PaintOpening(
                                id = openId,
                                type = OpeningType.fromString(openTypeStr),
                                width = openWidth,
                                height = openHeight,
                                quantity = openQty,
                                unit = MeasurementUnit.fromString(openUnitStr)
                            )
                        )
                    }
                }

                list.add(
                    CalculationRecord(
                        id = id,
                        timestamp = timestamp,
                        formattedDate = formattedDate,
                        wallLength = wallLength,
                        wallHeight = wallHeight,
                        unit = MeasurementUnit.fromString(unitStr),
                        grossWallAreaM2 = grossWallAreaM2,
                        openings = openingsList,
                        netPaintableAreaM2 = netPaintableAreaM2,
                        coats = coats,
                        coverageM2PerLiter = coverage,
                        wastePercent = waste,
                        basePaintLiters = basePaint,
                        estimatedPaintLiters = estimatedPaint,
                        canSizeLiters = canSize,
                        requiredCans = requiredCans
                    )
                )
            }
        } catch (_: Exception) {
            // Failsafe return partial or empty list on malformed json
        }
        return list
    }

    private fun serializeHistoryJson(list: List<CalculationRecord>): String {
        val jsonArray = JSONArray()
        for (record in list) {
            val obj = JSONObject()
            obj.put("id", record.id)
            obj.put("timestamp", record.timestamp)
            obj.put("formattedDate", record.formattedDate)
            obj.put("wallLength", record.wallLength)
            obj.put("wallHeight", record.wallHeight)
            obj.put("unit", record.unit.name)
            obj.put("grossWallAreaM2", record.grossWallAreaM2)
            obj.put("netPaintableAreaM2", record.netPaintableAreaM2)
            obj.put("coats", record.coats)
            obj.put("coverageM2PerLiter", record.coverageM2PerLiter)
            obj.put("wastePercent", record.wastePercent)
            obj.put("basePaintLiters", record.basePaintLiters)
            obj.put("estimatedPaintLiters", record.estimatedPaintLiters)
            if (record.canSizeLiters != null) {
                obj.put("canSizeLiters", record.canSizeLiters)
            } else {
                obj.put("canSizeLiters", JSONObject.NULL)
            }
            if (record.requiredCans != null) {
                obj.put("requiredCans", record.requiredCans)
            } else {
                obj.put("requiredCans", JSONObject.NULL)
            }

            val openingsArray = JSONArray()
            for (opening in record.openings) {
                val openObj = JSONObject()
                openObj.put("id", opening.id)
                openObj.put("type", opening.type.name)
                openObj.put("width", opening.width)
                openObj.put("height", opening.height)
                openObj.put("quantity", opening.quantity)
                openObj.put("unit", opening.unit.name)
                openingsArray.put(openObj)
            }
            obj.put("openings", openingsArray)

            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
