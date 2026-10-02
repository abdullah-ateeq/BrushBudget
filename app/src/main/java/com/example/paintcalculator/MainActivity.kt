package com.example.paintcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paintcalculator.ui.navigation.AppNavigation
import com.example.paintcalculator.ui.theme.PaintCalculatorTheme
import com.example.paintcalculator.ui.viewmodel.PaintCalculatorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PaintCalculatorViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            PaintCalculatorTheme(
                appTheme = uiState.appSettings.theme,
                dynamicColor = uiState.appSettings.dynamicColor
            ) {
                AppNavigation(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}