package com.example.paintcalculator.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

class SoundAndHapticHelper(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 30) // Low volume (30%)
        } catch (_: Exception) {
            // ToneGenerator initialization failure fallback
        }
    }

    fun triggerHaptic(composeHaptic: HapticFeedback? = null, enabled: Boolean) {
        if (!enabled) return
        try {
            if (composeHaptic != null) {
                composeHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun triggerSound(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun triggerActionFeedback(
        composeHaptic: HapticFeedback? = null,
        hapticsEnabled: Boolean,
        soundEnabled: Boolean
    ) {
        triggerHaptic(composeHaptic, hapticsEnabled)
        triggerSound(soundEnabled)
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {
        }
    }
}
