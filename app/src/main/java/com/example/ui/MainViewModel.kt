package com.example.ui

import android.app.Application
import android.content.Context
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
import java.io.File

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

  // Engines
  val ttsManager = TtsManager(application)
  val consoleEngine = VirtualConsoleEngine(viewModelScope)
  val aiHarness = AiAgentHarness(consoleEngine, ttsManager, viewModelScope)
  val adbBridge = AdbWifiBridge(viewModelScope)
  val pythonEngine = PythonAutomationEngine(consoleEngine, aiHarness, viewModelScope)

  // --------------------------------------------------------------------------
  // UNIFIED PAUSE & MENU CONTRACT: Gameplay pauses while menu is open
  // --------------------------------------------------------------------------

  fun openMenu() {
    consoleEngine.setPaused(true)
    _isMenuOpen.value = true
  }

  fun closeMenu() {
    consoleEngine.setPaused(false)
    _isMenuOpen.value = false
  }

  fun toggleMenu() {
    val newOpen = !_isMenuOpen.value
    consoleEngine.setPaused(newOpen)
    _isMenuOpen.value = newOpen
  }

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

  // --------------------------------------------------------------------------
  // INPUT CONTRACT: KeyDown, KeyUp, Simultaneous Inputs, Human Takeover
  // --------------------------------------------------------------------------

  fun onKeyDown(key: GamepadKey) {
    consoleEngine.onKeyDown(key)
    aiHarness.notifyHumanTakeover()
    aiHarness.recordUserGameplay(key.name)
  }

  fun onKeyUp(key: GamepadKey) {
    consoleEngine.onKeyUp(key)
  }

  fun sendGamepadInput(key: GamepadKey) {
    onKeyDown(key)
    onKeyUp(key)
  }

  fun onCirclePad(dx: Float, dy: Float) {
    consoleEngine.onCirclePad(dx, dy)
    aiHarness.notifyHumanTakeover()
  }

  fun onTouchDown(x: Float, y: Float) {
    consoleEngine.onTouchDown(x, y)
    aiHarness.notifyHumanTakeover()
  }

  fun onTouchUp() {
    consoleEngine.onTouchUp()
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

  fun exportDatasetFile(context: Context): File {
    return aiHarness.exportDatasetToFile(context)
  }

  override fun onCleared() {
    super.onCleared()
    ttsManager.shutdown()
  }
}
