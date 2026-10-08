package com.example.engine

import com.example.model.AgentBehaviorConfig
import com.example.model.AiGoal
import com.example.model.AiPersonality
import com.example.model.ChatMessage
import com.example.model.ControlMode
import com.example.model.DatasetRecord
import com.example.model.DatasetSampleType
import com.example.model.EdgeModel
import com.example.model.GamepadKey
import com.example.model.KnowledgeLevel
import com.example.model.LoraAdapter
import com.example.model.MessageSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * AI Agent Harness executing Google AI Edge models, LoRA policies, and TTS thoughts
 */
class AiAgentHarness(
  private val consoleEngine: VirtualConsoleEngine,
  private val ttsManager: TtsManager,
  private val scope: CoroutineScope
) {
  // Config state
  private val _config = MutableStateFlow(AgentBehaviorConfig())
  val config: StateFlow<AgentBehaviorConfig> = _config.asStateFlow()

  // Control mode
  private val _controlMode = MutableStateFlow(ControlMode.HUMAN)
  val controlMode: StateFlow<ControlMode> = _controlMode.asStateFlow()

  // Live Thought Spoken
  private val _currentThought = MutableStateFlow<String>("Initializing Edge model harness. Standing by in Littleroot Town.")
  val currentThought: StateFlow<String> = _currentThought.asStateFlow()

  // Thought Log history
  private val _thoughtHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
  val thoughtHistory: StateFlow<List<Pair<String, String>>> = _thoughtHistory.asStateFlow()

  // Available Edge Models
  private val _edgeModels = MutableStateFlow(
    listOf(
      EdgeModel("paligemma_3b_edge", "PaliGemma-3B (Vision-Language)", "SigLIP + Gemma Edge", 1850, 22, true, true),
      EdgeModel("gemma_2b_edge", "Gemma-2B-IT (Instruction)", "Gemma Transformer 2B", 1280, 14, false, false),
      EdgeModel("mobilenet_v4_game", "MobileNet-V4 GameState", "Universal Inverted Bottleneck", 120, 8, true, false),
      EdgeModel("smolvlm_edge", "SmolVLM-Edge (Ultra-Low Latency)", "SmolLM + Vision Encoder", 640, 16, true, false),
      EdgeModel("llama_3_2_1b", "Llama-3.2-1B Edge (Tactical)", "Meta Edge Core", 880, 18, false, false)
    )
  )
  val edgeModels: StateFlow<List<EdgeModel>> = _edgeModels.asStateFlow()

  // Available LoRA Adapters
  private val _loraAdapters = MutableStateFlow(
    listOf(
      LoraAdapter("pokemon_speedrun_v3", "Pokemon_Emerald_Any%_Speedrun.lora", "Pokémon Emerald", "Optimizes frame-skip routes, RNG manipulation and gym skips", 16, 28, true),
      LoraAdapter("battle_tactics_pro", "VGC_Battle_Mastery_v2.lora", "Pokémon Series", "Calculates super-effective damage, switch-outs and stat buffs", 32, 34, false),
      LoraAdapter("dungeon_puzzle_solver", "Zelda_Dungeon_Graph_Search.lora", "The Legend of Zelda", "Direct A* pathfinding and block puzzle logic", 16, 22, false),
      LoraAdapter("rng_manipulation_expert", "GBA_RNG_Seed_Manipulator.lora", "All GBA Titles", "Memory hooks to guarantee critical hits and shiny encounters", 64, 45, false),
      LoraAdapter("dialogue_parser_lora", "Japanese_English_Text_OCR_LoRA", "All Titles", "High-speed tokenization and intent extraction from text boxes", 8, 19, false)
    )
  )
  val loraAdapters: StateFlow<List<LoraAdapter>> = _loraAdapters.asStateFlow()

  // Goals
  private val _goals = MutableStateFlow(
    listOf(
      AiGoal(
        id = "g1",
        title = "Reach Oldale Town & Acquire Pokedex",
        description = "Navigate North through Route 101, survive wild encounters, meet May, and return to Prof. Birch.",
        progressPercent = 45,
        subTasks = listOf("Exit Littleroot Town (Done)", "Cross Route 101 grass patch (In Progress)", "Defeat May on Route 103", "Obtain Pokedex from Birch")
      ),
      AiGoal(
        id = "g2",
        title = "Defeat Gym Leader Roxanne in Rustboro City",
        description = "Train starter to level 14, navigate Petalburg Woods, and clear the Rock-type gym.",
        progressPercent = 10,
        subTasks = listOf("Acquire Running Shoes", "Navigate Petalburg Woods", "Battle Gym Trainers", "Earn Stone Badge")
      )
    )
  )
  val goals: StateFlow<List<AiGoal>> = _goals.asStateFlow()

  // Chat conversation
  private val _chatMessages = MutableStateFlow(
    listOf(
      ChatMessage(
        id = "m1",
        sender = MessageSender.SYSTEM,
        text = "EdgePilot Harness initialized with PaliGemma-3B + Pokemon_Speedrun LoRA. DMA Memory Mapping Active at 0x02000000.",
        timestamp = "13:00"
      ),
      ChatMessage(
        id = "m2",
        sender = MessageSender.AI_AGENT,
        text = "Ready to play! I have low-latency access to the input buffer and screen OCR. You can play manually, let me co-pilot, or give me full autonomous control.",
        timestamp = "13:01",
        actionTag = "Model: PaliGemma-3B"
      )
    )
  )
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  // Dataset records for fine-tuning
  private val _dataset = MutableStateFlow<List<DatasetRecord>>(emptyList())
  val dataset: StateFlow<List<DatasetRecord>> = _dataset.asStateFlow()

  // Performance telemetry
  private val _telemetry = MutableStateFlow(TelemetryData(fps = 59.8f, loopLatencyMs = 16, memoryMb = 482, decisionsPerMin = 142))
  val telemetry: StateFlow<TelemetryData> = _telemetry.asStateFlow()

  private var agentLoopJob: Job? = null

  init {
    startAgentDecisionLoop()
  }

  fun updateConfig(newConfig: AgentBehaviorConfig) {
    val previousConfig = _config.value
    _config.value = newConfig
    ttsManager.updateVoiceSettings(newConfig.ttsPitch, newConfig.ttsSpeechRate)

    if (previousConfig.personality != newConfig.personality) {
      val note = "Switched personality profile to ${newConfig.personality.displayName}. Speech tone: ${newConfig.personality.speechTone}."
      recordThought(note)
    }
  }

  fun setControlMode(mode: ControlMode) {
    _controlMode.value = mode
    val modeText = when (mode) {
      ControlMode.HUMAN -> "Handed controls back to human player. AI observing state."
      ControlMode.AI_CO_PILOT -> "AI Co-Pilot engaged. Assisting with navigation and battle heuristics."
      ControlMode.AI_AUTONOMOUS -> "AI Autonomous mode activated! Executing active goal sequence."
      ControlMode.AI_CHAOS_CHEAT -> "AI + Chaos Cheater mode enabled! Live memory manipulation unlocked."
    }
    recordThought(modeText)
  }

  fun selectModel(modelId: String) {
    _edgeModels.update { list ->
      list.map { it.copy(isLoaded = (it.id == modelId)) }
    }
    _config.update { it.copy(selectedModelId = modelId) }
    val chosen = _edgeModels.value.firstOrNull { it.id == modelId }?.name ?: modelId
    recordThought("Loaded Edge model: $chosen into NPU accelerator.")
  }

  fun toggleLora(loraId: String) {
    _loraAdapters.update { list ->
      list.map {
        if (it.id == loraId) it.copy(isApplied = !it.isApplied) else it
      }
    }
    val active = _loraAdapters.value.filter { it.isApplied }.map { it.name }
    recordThought("LoRA weights updated: ${active.joinToString(", ")}")
  }

  fun addCustomLora(name: String, targetGame: String, description: String) {
    val newLora = LoraAdapter(
      id = "custom_lora_${System.currentTimeMillis()}",
      name = name,
      targetScenario = targetGame,
      description = description,
      rank = 32,
      accuracyBonusPercent = 30,
      isApplied = true
    )
    _loraAdapters.update { listOf(newLora) + it }
    recordThought("Imported custom LoRA adapter: $name")
  }

  private fun startAgentDecisionLoop() {
    agentLoopJob?.cancel()
    agentLoopJob = scope.launch(Dispatchers.Default) {
      var decisionCounter = 0
      while (isActive) {
        val delayTime = _config.value.maxActionDelayMs.toLong().coerceIn(200L, 1000L)
        delay(delayTime)

        val mode = _controlMode.value
        val state = consoleEngine.gameState.value

        // Only act if in Co-Pilot, Autonomous, or Chaos mode
        if (mode == ControlMode.AI_AUTONOMOUS || mode == ControlMode.AI_CHAOS_CHEAT ||
          (mode == ControlMode.AI_CO_PILOT && Random.nextFloat() < 0.4f)) {

          decisionCounter++
          executeAgentAction(state, mode)

          // Periodically speak out a thought (every 4-7 decisions)
          if (decisionCounter % 5 == 0) {
            generateContextualThought(state)
          }

          // Telemetry jitter
          _telemetry.update {
            it.copy(
              fps = 58.5f + Random.nextFloat() * 2.5f,
              loopLatencyMs = (14 + Random.nextInt(12)),
              decisionsPerMin = 130 + Random.nextInt(25)
            )
          }
        }
      }
    }
  }

  private fun executeAgentAction(state: com.example.model.GameStateSnapshot, mode: ControlMode) {
    val personality = _config.value.personality
    val isChaos = (mode == ControlMode.AI_CHAOS_CHEAT) || (personality == AiPersonality.CHAOS_CHEATER && _config.value.memoryCheatsAllowed)

    // Check if Chaos Cheater wants to inject a memory poke!
    if (isChaos && Random.nextFloat() < 0.20f) {
      triggerChaosMemoryHack(state)
      return
    }

    if (state.isInBattle) {
      // In battle, press A to attack or B if low HP
      if (state.playerHp < 8 && personality == AiPersonality.STRATEGIST) {
        consoleEngine.sendInput(GamepadKey.B) // Retreat safely
        recordThought("Low HP detected (${state.playerHp}/${state.playerMaxHp}). Retreating safely to preserve team.")
      } else {
        consoleEngine.sendInput(GamepadKey.A)
      }
    } else {
      // Overworld movement based on goal
      val keys = when (personality) {
        AiPersonality.SPEEDRUNNER -> listOf(GamepadKey.UP, GamepadKey.UP, GamepadKey.RIGHT, GamepadKey.A)
        AiPersonality.LORE_EXPLORER -> listOf(GamepadKey.LEFT, GamepadKey.RIGHT, GamepadKey.UP, GamepadKey.A)
        AiPersonality.METHODICAL_GRINDER -> listOf(GamepadKey.UP, GamepadKey.DOWN, GamepadKey.UP, GamepadKey.DOWN)
        else -> listOf(GamepadKey.UP, GamepadKey.RIGHT, GamepadKey.DOWN, GamepadKey.LEFT, GamepadKey.A)
      }
      val chosenKey = keys.random()
      consoleEngine.sendInput(chosenKey)

      // Record dataset entry
      recordDatasetSample(state, chosenKey.name)
    }
  }

  private fun triggerChaosMemoryHack(state: com.example.model.GameStateSnapshot) {
    val hacks = listOf(
      "POKE_HP" to {
        consoleEngine.pokeMemory("0x0202402C", "0x00FF")
        recordThought("Chaos hack: Poked 0x0202402C with 255 HP! Unstoppable now.")
      },
      "POKE_MONEY" to {
        consoleEngine.pokeMemory("0x02024090", "0x000F423F")
        recordThought("Chaos hack: Injected 999,999 poke dollars into memory address 0x02024090.")
      },
      "ACTIVATE_SPEED" to {
        consoleEngine.toggleCheat("c5")
        recordThought("Chaos hack: 4x Turbo step multiplier active in virtual RAM.")
      }
    )
    val chosen = hacks.random()
    chosen.second.invoke()
  }

  private fun generateContextualThought(state: com.example.model.GameStateSnapshot) {
    val personality = _config.value.personality
    val knowledge = _config.value.knowledgeLevel
    val thought = when {
      state.isInBattle -> {
        when (personality) {
          AiPersonality.SPEEDRUNNER -> "Enemy battle detected! Spamming A to minimize text frames. Target HP: ${state.enemyHp}."
          AiPersonality.STRATEGIST -> "Analyzing ${state.enemyName ?: "target"} defenses. Our level is ${state.playerLevel}; optimal attack selected."
          AiPersonality.CHAOS_CHEATER -> "Why fight fair? Checking if I can poke enemy HP to zero at offset 0x02024040!"
          AiPersonality.LORE_EXPLORER -> "Encountered ${state.enemyName}! Documenting regional species data."
          AiPersonality.METHODICAL_GRINDER -> "Combat engaged. Good EXP source for leveling up our starter."
        }
      }
      state.dialogText.isNotBlank() -> {
        when (personality) {
          AiPersonality.SPEEDRUNNER -> "Dialogue box parsed by OCR. Mashed B+A to skip in 2 frames."
          AiPersonality.LORE_EXPLORER -> "NPC dialogue: '${state.dialogText.take(30)}...' Very interesting world lore!"
          else -> "Reading game prompt: '${state.dialogText.take(30)}...'"
        }
      }
      else -> {
        when (personality) {
          AiPersonality.SPEEDRUNNER -> "Moving toward Route 101 north coordinate. Tile (${state.playerX}, ${state.playerY}). Frame rate rock solid at 60 FPS."
          AiPersonality.STRATEGIST -> "Checking inventory and path coordinates. Safe zone maintained."
          AiPersonality.CHAOS_CHEATER -> "RAM state looks juicy. Memory watch has 8 live registers tracked."
          AiPersonality.LORE_EXPLORER -> "Surveying ${state.zoneName}. Looking for hidden Poke Balls in tree tiles."
          AiPersonality.METHODICAL_GRINDER -> "Pacing grass borders to optimize encounter rate and grind XP."
        }
      }
    }
    recordThought(thought)
  }

  fun recordThought(text: String) {
    _currentThought.value = text
    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    _thoughtHistory.update { (listOf(time to text) + it).take(50) }

    // Speak aloud using TTS if enabled
    if (_config.value.ttsEnabled) {
      ttsManager.speak(text)
    }
  }

  fun sendUserChatMessage(userText: String) {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val userMsg = ChatMessage(
      id = "user_${System.currentTimeMillis()}",
      sender = MessageSender.USER,
      text = userText,
      timestamp = time
    )
    _chatMessages.update { it + userMsg }

    // Generate intelligent AI response based on game state, personality and instruction
    scope.launch(Dispatchers.Default) {
      delay(400) // Brief processing delay for realistic inference feel
      val reply = processUserQuery(userText)
      _chatMessages.update { it + reply }

      // Also speak the response if TTS is on
      if (_config.value.ttsEnabled) {
        ttsManager.speak(reply.text)
      }
    }
  }

  private fun processUserQuery(query: String): ChatMessage {
    val q = query.lowercase(Locale.ROOT)
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val state = consoleEngine.gameState.value
    val personality = _config.value.personality

    return when {
      q.contains("why") && (q.contains("move") || q.contains("grass") || q.contains("step")) -> {
        ChatMessage(
          id = "ai_${System.currentTimeMillis()}",
          sender = MessageSender.AI_AGENT,
          text = "According to my current policy (${_config.value.personality.displayName}), stepping through tile (${state.playerX}, ${state.playerY}) saves 14 frames compared to the paved path. My vision overlay detected Route 101 waypoint at confidence 0.94.",
          timestamp = time,
          actionTag = "Pathing Decision"
        )
      }
      q.contains("cheat") || q.contains("money") || q.contains("hp") || q.contains("hack") -> {
        if (_config.value.memoryCheatsAllowed) {
          consoleEngine.toggleCheat("c3")
          ChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            sender = MessageSender.AI_AGENT,
            text = "Done! I accessed RAM offset 0x02024090 and set our funds to $999,999. Live memory poke executed with zero desync.",
            timestamp = time,
            actionTag = "Memory Poke Success",
            memoryPokeSnippet = "poke(0x02024090, 0x000F423F)"
          )
        } else {
          ChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            sender = MessageSender.AI_AGENT,
            text = "Memory cheating is currently disabled in your AI Config Dashboard. Toggle 'Memory Cheats Allowed' in settings to let me poke the game RAM!",
            timestamp = time,
            actionTag = "Permission Guard"
          )
        }
      }
      q.contains("goal") || q.contains("beat") || q.contains("defeat") || q.contains("gym") -> {
        val newGoal = AiGoal(
          id = "g_${System.currentTimeMillis()}",
          title = "User Assigned: $query",
          description = "Custom tactical objective assigned via chat interface.",
          progressPercent = 5,
          subTasks = listOf("Scan game environment", "Optimize battle loadout", "Execute action routine")
        )
        _goals.update { listOf(newGoal) + it }
        ChatMessage(
          id = "ai_${System.currentTimeMillis()}",
          sender = MessageSender.AI_AGENT,
          text = "Understood! I've created a new active goal: '${newGoal.title}'. Updating my decision tree and route weights to prioritize this immediately.",
          timestamp = time,
          actionTag = "New Goal Created"
        )
      }
      q.contains("web") || q.contains("search") || q.contains("wiki") -> {
        if (_config.value.webSearchEnabled) {
          ChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            sender = MessageSender.AI_AGENT,
            text = "Searching Bulbapedia / Strategy Wiki: 'Roxanne's team consists of Geodude (Lv 12, Rock Throw) and Nosepass (Lv 15, Rock Tomb). Weak to Water and Grass types.' Recommendation: Lead with Mudkip/Treecko.",
            timestamp = time,
            actionTag = "Web Knowledge Search"
          )
        } else {
          ChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            sender = MessageSender.AI_AGENT,
            text = "Web Search access is toggled off in settings. Enable 'Web Access' in the AI Config Dashboard so I can fetch live wikis and damage calculators.",
            timestamp = time
          )
        }
      }
      else -> {
        val personalityQuirk = when (personality) {
          AiPersonality.SPEEDRUNNER -> "Let's keep up the pace! I'm monitoring sub-pixel alignment and input buffers."
          AiPersonality.STRATEGIST -> "I'm calculating optimal turn probabilities based on current HP (${state.playerHp}/${state.playerMaxHp})."
          AiPersonality.CHAOS_CHEATER -> "I've got the memory editor hooked. We can bend any rule you want in this ROM!"
          AiPersonality.LORE_EXPLORER -> "I love exploring this region. Have you noticed the background tile details in ${state.zoneName}?"
          AiPersonality.METHODICAL_GRINDER -> "Ready to grind XP or complete any task you need."
        }
        ChatMessage(
          id = "ai_${System.currentTimeMillis()}",
          sender = MessageSender.AI_AGENT,
          text = "Got it! Current state: Zone '${state.zoneName}', Player HP ${state.playerHp}/${state.playerMaxHp}, Coins \$${state.coins}. $personalityQuirk",
          timestamp = time,
          actionTag = "Status Sync"
        )
      }
    }
  }

  fun addNewGoal(title: String, description: String, subtasks: List<String>) {
    val newGoal = AiGoal(
      id = "g_${System.currentTimeMillis()}",
      title = title,
      description = description,
      progressPercent = 0,
      subTasks = subtasks
    )
    _goals.update { listOf(newGoal) + it }
    recordThought("Assigned new agent goal: $title")
  }

  fun updateGoalProgress(goalId: String, newPercent: Int) {
    _goals.update { list ->
      list.map {
        if (it.id == goalId) it.copy(progressPercent = newPercent, isCompleted = (newPercent >= 100)) else it
      }
    }
  }

  private val _isRecordingEnabled = MutableStateFlow(true)
  val isRecordingEnabled: StateFlow<Boolean> = _isRecordingEnabled.asStateFlow()

  fun toggleGameplayRecording() {
    _isRecordingEnabled.value = !_isRecordingEnabled.value
    recordThought("Gameplay Training Recorder: ${if (_isRecordingEnabled.value) "ACTIVE (Recording fights, nav & screenshots)" else "PAUSED"}")
  }

  fun recordUserGameplay(input: String) {
    if (!_isRecordingEnabled.value) return
    val state = consoleEngine.gameState.value
    val sampleType = when {
      state.isInBattle -> DatasetSampleType.FIGHT
      input in listOf("UP", "DOWN", "LEFT", "RIGHT") -> DatasetSampleType.NAVIGATION
      else -> DatasetSampleType.DECISION
    }
    recordDatasetSample(state, input, sampleType)
  }

  fun captureScreenshot(reason: String = "Manual Capture") {
    val state = consoleEngine.gameState.value
    val record = DatasetRecord(
      id = "ds_shot_${System.currentTimeMillis()}",
      timestamp = System.currentTimeMillis(),
      scenario = state.scenario.title,
      sampleType = DatasetSampleType.SCREENSHOT,
      frameNumber = state.frameNumber,
      playerCoords = state.playerX to state.playerY,
      zoneName = state.zoneName,
      inBattle = state.isInBattle,
      opponentName = state.enemyName,
      detectedObjects = state.detections.size,
      memorySnapshotHash = "MEM_" + Integer.toHexString(state.playerX * 31 + state.playerY),
      inputCommand = state.lastInputKey,
      agentReasoning = "Screenshot: $reason | ${state.zoneName} (${state.playerX},${state.playerY})",
      screenshotTag = "SCREENSHOT_${state.scenario.id.uppercase()}_F${state.frameNumber}.PNG"
    )
    _dataset.update { (listOf(record) + it).take(250) }
    recordThought("Captured training frame: ${record.screenshotTag}")
  }

  fun clearDataset() {
    _dataset.value = emptyList()
    recordThought("Gameplay training dataset buffer cleared.")
  }

  fun exportDatasetJsonl(): String {
    val items = _dataset.value
    return items.joinToString("\n") { r ->
      """{"id":"${r.id}","timestamp":${r.timestamp},"scenario":"${r.scenario}","type":"${r.sampleType.name}","frame":${r.frameNumber},"coords":[${r.playerCoords.first},${r.playerCoords.second}],"zone":"${r.zoneName}","in_battle":${r.inBattle},"opponent":"${r.opponentName ?: ""}","input":"${r.inputCommand}","reasoning":"${r.agentReasoning.replace("\"", "\\\"")}","screenshot":"${r.screenshotTag ?: ""}"}"""
    }
  }

  private fun recordDatasetSample(state: com.example.model.GameStateSnapshot, input: String, type: DatasetSampleType = DatasetSampleType.NAVIGATION) {
    val record = DatasetRecord(
      id = "ds_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
      timestamp = System.currentTimeMillis(),
      scenario = state.scenario.title,
      sampleType = type,
      frameNumber = state.frameNumber,
      playerCoords = state.playerX to state.playerY,
      zoneName = state.zoneName,
      inBattle = state.isInBattle,
      opponentName = state.enemyName,
      detectedObjects = state.detections.size,
      memorySnapshotHash = "MEM_" + Integer.toHexString(state.playerX * 31 + state.playerY),
      inputCommand = input,
      agentReasoning = _currentThought.value.take(64),
      screenshotTag = if (Random.nextInt(10) < 2) "RANDOM_SNAP_${state.frameNumber}.PNG" else null,
      isFlaggedIssue = state.isInBattle && state.playerHp < 5
    )
    _dataset.update { (listOf(record) + it).take(250) }
  }
}

data class TelemetryData(
  val fps: Float,
  val loopLatencyMs: Int,
  val memoryMb: Int,
  val decisionsPerMin: Int
)
