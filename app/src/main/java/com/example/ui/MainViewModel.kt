package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AdbWifiBridge
import com.example.engine.AiAgentHarness
import com.example.engine.CheatEngineManager
import com.example.engine.DevToolsEngine
import com.example.engine.GrindBotEngine
import com.example.engine.HapticFeedbackManager
import com.example.engine.ModEngineManager
import com.example.engine.PythonAutomationEngine
import com.example.engine.TtsManager
import com.example.engine.VirtualConsoleEngine
import com.example.model.AgentBehaviorConfig
import com.example.model.AiPersonality
import com.example.model.ButtonColorPreset
import com.example.model.CheatCodeEntry
import com.example.model.CheatEngineType
import com.example.model.ConsoleCustomizationConfig
import com.example.model.ControlMode
import com.example.model.GameConsoleMode
import com.example.model.GamepadKey
import com.example.model.GameScenario
import com.example.model.GrindBotGoal
import com.example.model.HapticProfile
import com.example.model.KnowledgeLevel
import com.example.model.ShellColorPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

enum class NavigationTab(val title: String, val icon: String) {
  HARNESS("Console", "Gamepad"),
  GRIND_BOT("Grind Bot", "Bot"),
  CUSTOMIZE("Chassis & Keys", "Palette"),
  CHEATS_CODES("Cheats & Codes", "Code"),
  MOD_HUB("Mods & Hacks", "Extension"),
  DEV_TOOLS("Dev Tools", "BugReport"),
  MEMORY_CHEATS("RAM Watch", "Memory"),
  AI_CONFIG("AI Config", "Psychology"),
  CHAT_GOALS("Chat & Goals", "Chat"),
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

  // Console Shell, Buttons, and Haptic Customization
  private val _customizationConfig = MutableStateFlow(ConsoleCustomizationConfig())
  val customizationConfig: StateFlow<ConsoleCustomizationConfig> = _customizationConfig.asStateFlow()

  // Core Engines & Subsystems
  val ttsManager = TtsManager(application)
  val hapticManager = HapticFeedbackManager(application)
  val consoleEngine = VirtualConsoleEngine(viewModelScope)
  val aiHarness = AiAgentHarness(consoleEngine, ttsManager, viewModelScope)
  val adbBridge = AdbWifiBridge(viewModelScope)
  val pythonEngine = PythonAutomationEngine(consoleEngine, aiHarness, viewModelScope)

  // Dedicated Pokémon AI Grind Bot & Cheat/Mod/Dev Tools
  val grindBot = GrindBotEngine(consoleEngine, ttsManager, hapticManager, viewModelScope)
  val cheatEngine = CheatEngineManager(consoleEngine)
  val modEngine = ModEngineManager(consoleEngine)
  val devTools = DevToolsEngine(consoleEngine)

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
  // INPUT CONTRACT WITH PHYSICAL HAPTIC FEEDBACK
  // --------------------------------------------------------------------------

  fun onKeyDown(key: GamepadKey) {
    hapticManager.triggerPress(_customizationConfig.value)
    consoleEngine.onKeyDown(key)
    aiHarness.notifyHumanTakeover()
    aiHarness.recordUserGameplay(key.name)
  }

  fun onKeyUp(key: GamepadKey) {
    hapticManager.triggerRelease(_customizationConfig.value)
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
    hapticManager.triggerTouch(_customizationConfig.value)
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

  // --------------------------------------------------------------------------
  // SHELL & BUTTON CUSTOMIZATION & HAPTIC CONTROLS
  // --------------------------------------------------------------------------

  fun setShellPreset(preset: ShellColorPreset) {
    _customizationConfig.update {
      it.copy(
        shellPreset = preset,
        isCustomColorActive = false,
        customShellHex = preset.shellBaseHex,
        customShellHighlightHex = preset.shellHighlightHex,
        customShellDeepHex = preset.shellDeepHex
      )
    }
  }

  fun setButtonPreset(preset: ButtonColorPreset) {
    _customizationConfig.update {
      it.copy(
        buttonPreset = preset,
        customButtonFaceHex = preset.buttonFaceHex,
        customButtonTextHex = preset.buttonTextHex,
        customDpadHex = preset.dpadHex
      )
    }
  }

  fun setCustomShellColors(baseHex: Long, highlightHex: Long, deepHex: Long) {
    _customizationConfig.update {
      it.copy(
        isCustomColorActive = true,
        customShellHex = baseHex,
        customShellHighlightHex = highlightHex,
        customShellDeepHex = deepHex
      )
    }
  }

  fun setCustomButtonColors(faceHex: Long, textHex: Long, dpadHex: Long) {
    _customizationConfig.update {
      it.copy(
        customButtonFaceHex = faceHex,
        customButtonTextHex = textHex,
        customDpadHex = dpadHex
      )
    }
  }

  fun setHapticProfile(profile: HapticProfile) {
    _customizationConfig.update { it.copy(hapticProfile = profile) }
  }

  fun toggleHapticOnPress() {
    _customizationConfig.update { it.copy(hapticOnPress = !it.hapticOnPress) }
  }

  fun toggleHapticOnRelease() {
    _customizationConfig.update { it.copy(hapticOnRelease = !it.hapticOnRelease) }
  }

  fun toggleHapticOnTouch() {
    _customizationConfig.update { it.copy(hapticOnTouchScreen = !it.hapticOnTouchScreen) }
  }

  // --------------------------------------------------------------------------
  // AI GRIND BOT DELEGATES
  // --------------------------------------------------------------------------

  fun startGrindBot(goal: GrindBotGoal) {
    grindBot.startBot(goal)
  }

  fun stopGrindBot() {
    grindBot.stopBot()
  }

  fun setGrindGoal(goal: GrindBotGoal) {
    grindBot.setGoal(goal)
  }

  fun forceShinyEncounter() {
    grindBot.forceShinyEncounter()
  }

  // --------------------------------------------------------------------------
  // CHEAT ENGINE & MOD DELEGATES
  // --------------------------------------------------------------------------

  fun toggleCheatCode(cheatId: String): Result<Boolean> {
    val allowCheats = aiHarness.config.value.memoryCheatsAllowed
    return cheatEngine.toggleCheat(cheatId, allowCheats)
  }

  fun addCustomCheat(
    title: String,
    code: String,
    type: CheatEngineType,
    desc: String,
    scenario: GameScenario?
  ): Result<CheatCodeEntry> {
    return cheatEngine.addNewCheat(title, code, type, desc, scenario)
  }

  fun testCheat(cheat: CheatCodeEntry): Result<String> {
    val allowCheats = aiHarness.config.value.memoryCheatsAllowed
    return cheatEngine.testCheatWrite(cheat, allowCheats)
  }

  fun toggleMod(modId: String): Boolean {
    return modEngine.toggleMod(modId)
  }

  fun toggleGameplayRule(ruleId: String): Boolean {
    return modEngine.toggleGameplayRule(ruleId)
  }

  // --------------------------------------------------------------------------
  // AGENT CONFIG & DATASET
  // --------------------------------------------------------------------------

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
