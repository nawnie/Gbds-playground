package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AdbWifiBridge
import com.example.engine.AiAgentHarness
import com.example.engine.PythonAutomationEngine
import com.example.engine.TtsManager
import com.example.engine.VirtualConsoleEngine
import com.example.model.AgentBehaviorConfig
import com.example.model.AiPersonality
import com.example.model.ControlMode
import com.example.model.GameConsoleMode
import com.example.model.GamepadKey
import com.example.model.GameScenario
import com.example.model.KnowledgeLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NavigationTab(val title: String, val icon: String) {
  HARNESS("Console", "Gamepad"),
  AI_CONFIG("AI Config", "Psychology"),
  CHAT_GOALS("Chat & Goals", "Chat"),
  MEMORY_CHEATS("RAM & Cheats", "Memory"),
  PYTHON_TASKS("Python", "Terminal"),
  ADB_REMOTE("ADB / Remote", "Wifi")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

  // Selected App Tab
  private val _currentTab = MutableStateFlow(NavigationTab.HARNESS)
  val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

  // Screen Overlay Toggle (Computer Vision Bounding Boxes)
  private val _showVisionOverlay = MutableStateFlow(true)
  val showVisionOverlay: StateFlow<Boolean> = _showVisionOverlay.asStateFlow()

  // Thought Bubble Audio Mute Toggle
  private val _ttsAudioMuted = MutableStateFlow(false)
  val ttsAudioMuted: StateFlow<Boolean> = _ttsAudioMuted.asStateFlow()

  // Menu Open State (Full realistic console until MENU is pressed)
  private val _isMenuOpen = MutableStateFlow(false)
  val isMenuOpen: StateFlow<Boolean> = _isMenuOpen.asStateFlow()

  fun openMenu() {
    _isMenuOpen.value = true
  }

  fun closeMenu() {
    _isMenuOpen.value = false
  }

  fun toggleMenu() {
    _isMenuOpen.value = !_isMenuOpen.value
  }

  // Engines
  val ttsManager = TtsManager(application)
  val consoleEngine = VirtualConsoleEngine(viewModelScope)
  val aiHarness = AiAgentHarness(consoleEngine, ttsManager, viewModelScope)
  val adbBridge = AdbWifiBridge(viewModelScope)
  val pythonEngine = PythonAutomationEngine(consoleEngine, aiHarness, viewModelScope)

  fun setTab(tab: NavigationTab) {
    _currentTab.value = tab
  }

  fun toggleVisionOverlay() {
    _showVisionOverlay.value = !_showVisionOverlay.value
  }

  fun toggleTtsMute() {
    val newMute = !_ttsAudioMuted.value
    _ttsAudioMuted.value = newMute
    if (newMute) {
      ttsManager.stop()
    } else {
      aiHarness.recordThought("TTS Voice narration active. I'll speak my tactical thoughts aloud.")
    }
  }

  fun sendGamepadInput(key: GamepadKey) {
    consoleEngine.sendInput(key)
    aiHarness.recordUserGameplay(key.name)
  }

  fun captureScreenshot() {
    aiHarness.captureScreenshot("User Snapshot")
  }

  fun toggleGameplayRecording() {
    aiHarness.toggleGameplayRecording()
  }

  fun setControlMode(mode: ControlMode) {
    aiHarness.setControlMode(mode)
  }

  fun switchGameScenario(scenario: GameScenario) {
    consoleEngine.switchScenario(scenario)
    aiHarness.recordThought("Cartridge swapped to: ${scenario.title}. Memory base initialized.")
  }

  fun switchConsoleMode(mode: GameConsoleMode) {
    consoleEngine.switchConsoleMode(mode)
  }

  fun updatePersonality(personality: AiPersonality) {
    val current = aiHarness.config.value
    aiHarness.updateConfig(current.copy(personality = personality))
  }

  fun updateKnowledgeLevel(level: KnowledgeLevel) {
    val current = aiHarness.config.value
    aiHarness.updateConfig(current.copy(knowledgeLevel = level))
  }

  fun updateConfig(config: AgentBehaviorConfig) {
    aiHarness.updateConfig(config)
  }

  override fun onCleared() {
    super.onCleared()
    ttsManager.shutdown()
  }
}
