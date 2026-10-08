package com.example.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Android native TextToSpeech manager for live agent spoken thoughts
 */
class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {
  private val tag = "TtsManager"
  private var textToSpeech: TextToSpeech? = null
  private var isInitialized = false

  private val _lastSpokenText = MutableStateFlow<String?>(null)
  val lastSpokenText: StateFlow<String?> = _lastSpokenText.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _speechRate = MutableStateFlow(1.0f)
  private val _pitch = MutableStateFlow(1.0f)

  init {
    try {
      textToSpeech = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      Log.e(tag, "Failed to initialize TextToSpeech engine", e)
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val result = textToSpeech?.setLanguage(Locale.US)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        Log.w(tag, "Language US not directly supported, falling back to default")
        textToSpeech?.setLanguage(Locale.getDefault())
      }
      isInitialized = true
      textToSpeech?.setSpeechRate(_speechRate.value)
      textToSpeech?.setPitch(_pitch.value)
      Log.d(tag, "TextToSpeech successfully initialized")
    } else {
      Log.e(tag, "TextToSpeech init failed with status: $status")
    }
  }

  fun updateVoiceSettings(pitch: Float, rate: Float) {
    _pitch.value = pitch.coerceIn(0.5f, 2.0f)
    _speechRate.value = rate.coerceIn(0.5f, 2.0f)
    if (isInitialized) {
      textToSpeech?.setPitch(_pitch.value)
      textToSpeech?.setSpeechRate(_speechRate.value)
    }
  }

  fun speak(text: String, isMuted: Boolean = false) {
    _lastSpokenText.value = text
    if (isMuted || !isInitialized || text.isBlank()) {
      return
    }

    try {
      _isSpeaking.value = true
      textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ai_thought_${System.currentTimeMillis()}")
    } catch (e: Exception) {
      Log.e(tag, "Error during TTS speak", e)
    }
  }

  fun stop() {
    try {
      textToSpeech?.stop()
      _isSpeaking.value = false
    } catch (e: Exception) {
      Log.e(tag, "Error stopping TTS", e)
    }
  }

  fun shutdown() {
    try {
      textToSpeech?.stop()
      textToSpeech?.shutdown()
      textToSpeech = null
      isInitialized = false
    } catch (e: Exception) {
      Log.e(tag, "Error shutting down TTS", e)
    }
  }
}
