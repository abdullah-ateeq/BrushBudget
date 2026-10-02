package com.example.paintcalculator.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onAnimationComplete: () -> Unit
) {
    val wallOutlineProgress = remember { Animatable(0f) }
    val paintFillProgress = remember { Animatable(0f) }
    val rollerStrokeProgress = remember { Animatable(0f) }
    val textAlphaProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Wall outline appears (0 to 400ms)
        launch {
            wallOutlineProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        // Step 2: Paint coverage area highlights + roller stroke (300 to 900ms)
        delay(250)
        launch {
            paintFillProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rollerStrokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = LinearEasing)
            )
        }
        // Step 3: Liters result & Title fade in (700 to 1200ms)
        delay(450)
        launch {
            textAlphaProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }

        // Total duration ~1.35 seconds
        delay(650)
        onAnimationComplete()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Canvas Illustration
            Canvas(
                modifier = Modifier.size(160.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val wallLeft = canvasWidth * 0.15f
                val wallTop = canvasHeight * 0.18f
                val wallWidth = canvasWidth * 0.70f
                val wallHeight = canvasHeight * 0.64f

                // 1. Wall outline
                if (wallOutlineProgress.value > 0f) {
                    drawRoundRect(
                        color = outlineColor.copy(alpha = 0.6f * wallOutlineProgress.value),
                        topLeft = Offset(wallLeft, wallTop),
                        size = Size(wallWidth, wallHeight),
                        cornerRadius = CornerRadius(12f, 12f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 2. Paint fill highlight
                if (paintFillProgress.value > 0f) {
                    val fillWidth = (wallWidth - 10f) * paintFillProgress.value
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.22f),
                        topLeft = Offset(wallLeft + 5f, wallTop + 5f),
                        size = Size(fillWidth, wallHeight - 10f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }

                // 3. Paint roller stroke accent
                if (rollerStrokeProgress.value > 0f) {
                    val strokeX = wallLeft + 5f + ((wallWidth - 10f) * rollerStrokeProgress.value)
                    drawLine(
                        color = secondaryColor,
                        start = Offset(strokeX, wallTop + 8f),
                        end = Offset(strokeX, wallTop + wallHeight - 8f),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 4. Door cut-out accent inside wall
                if (wallOutlineProgress.value > 0.5f) {
                    val doorWidth = wallWidth * 0.28f
                    val doorHeight = wallHeight * 0.52f
                    val doorLeft = wallLeft + wallWidth * 0.55f
                    val doorTop = wallTop + wallHeight - doorHeight

                    drawRoundRect(
                        color = surfaceColor,
                        topLeft = Offset(doorLeft, doorTop),
                        size = Size(doorWidth, doorHeight),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Title and Subtitle with fade-in
            Text(
                text = "BRUSHBUDGET",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = textAlphaProgress.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Professional Wall & Paint Estimator",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = textAlphaProgress.value)
            )
        }
    }
}
