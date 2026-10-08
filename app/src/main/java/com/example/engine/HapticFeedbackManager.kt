package com.example.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.model.ConsoleCustomizationConfig
import com.example.model.HapticProfile

/**
 * High-precision handheld haptic feedback engine.
 * Mimics physical tactile switches, membrane resistance, and celebratory alerts.
 */
class HapticFeedbackManager(context: Context) {
  private val vibrator: Vibrator? = try {
    context.getSystemService(Vibrator::class.java)
  } catch (_: Exception) {
    null
  }

  fun triggerPress(config: ConsoleCustomizationConfig) {
    if (!config.hapticOnPress || config.hapticProfile == HapticProfile.OFF) return
    executeVibration(config.hapticProfile.durationMs, config.hapticProfile.amplitude)
  }

  fun triggerRelease(config: ConsoleCustomizationConfig) {
    if (!config.hapticOnRelease || config.hapticProfile == HapticProfile.OFF) return
    // Very light release tick
    val releaseDuration = (config.hapticProfile.durationMs / 2).coerceAtLeast(4)
    val releaseAmplitude = (config.hapticProfile.amplitude / 2).coerceAtLeast(30)
    executeVibration(releaseDuration, releaseAmplitude)
  }

  fun triggerTouch(config: ConsoleCustomizationConfig) {
    if (!config.hapticOnTouchScreen || config.hapticProfile == HapticProfile.OFF) return
    executeVibration(8, 90)
  }

  fun triggerShinyAlert() {
    // Triple celebratory pulse: buzz-buzz-BUZZ
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val timings = longArrayOf(0, 60, 50, 60, 50, 180)
        val amplitudes = intArrayOf(0, 200, 0, 220, 0, 255)
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(300)
      }
    } catch (_: Exception) {}
  }

  fun triggerCheatToggle() {
    executeVibration(18, 140)
  }

  private fun executeVibration(durationMs: Long, amplitude: Int) {
    if (vibrator == null || durationMs <= 0) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(durationMs)
      }
    } catch (_: Exception) {}
  }
}
