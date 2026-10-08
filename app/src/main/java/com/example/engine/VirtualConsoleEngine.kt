package com.example.engine

import com.example.model.GameCheat
import com.example.model.GameConsoleMode
import com.example.model.GamepadKey
import com.example.model.GameScenario
import com.example.model.GameStateSnapshot
import com.example.model.InputOwner
import com.example.model.MemoryWatchEntry
import com.example.model.TouchPoint
import com.example.model.VisionCategory
import com.example.model.VisionDetection
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
import kotlin.random.Random

/**
 * Emulated Virtual Console Engine for GBA and 3DS systems.
 * Provides a real input contract with simultaneous keypresses, held states,
 * release events, touch-screen digitizer, pause/takeover enforcement, and memory gatekeeping.
 */
class VirtualConsoleEngine(
  private val scope: CoroutineScope
) {
  private val _gameState = MutableStateFlow(
    GameStateSnapshot(
      scenario = GameScenario.POKEMON_RED,
      consoleMode = GameConsoleMode.GBA,
      zoneName = "Pallet Town - Route 1",
      dialogText = "Now tell me. Are you a boy or a girl?"
    )
  )
  val gameState: StateFlow<GameStateSnapshot> = _gameState.asStateFlow()

  // Real-time Memory Watch Map
  private val _memoryMap = MutableStateFlow<Map<String, Int>>(
    mapOf(
      "0x02024284" to 12,    // Player X
      "0x02024286" to 18,    // Player Y
      "0x0202402C" to 20,    // Player HP
      "0x0202402E" to 20,    // Player Max HP
      "0x02024090" to 1000,  // Money / Coins
      "0x02024036" to 5,     // Player Level
      "0x020386E0" to 0,     // Battle State Flag (0 = Field, 1 = Battle)
      "0x03004020" to 0x03FF // Key Input Buffer (GBA active-low 10-bit register)
    )
  )

  // Active Cheat Switches
  private val _cheats = MutableStateFlow<List<GameCheat>>(
    listOf(
      GameCheat("c1", "Infinite HP / God Mode", "Locks Player HP to Max HP permanently", "0x0202402C", "0x0014", false, "STATS"),
      GameCheat("c2", "Walk Through Walls (Ghost Clip)", "Disables tile collision checks in RAM", "0x020370E4", "0x0001", false, "MOVEMENT"),
      GameCheat("c3", "Max Money ($999,999)", "Pokes money offset to maximum value", "0x02024090", "0x000F423F", false, "RESOURCES"),
      GameCheat("c4", "One-Hit KO in Battles", "Reduces opponent HP to 0 on attack", "0x02024040", "0x0000", false, "BATTLE"),
      GameCheat("c5", "4x Turbo Movement Speed", "Overclocks step cycle speed in RAM", "0x02038010", "0x0004", false, "MOVEMENT"),
      GameCheat("c6", "Infinite Master Balls", "Injects 99 Master Balls into Bag offset", "0x02025800", "0x0063", false, "RESOURCES")
    )
  )
  val cheats: StateFlow<List<GameCheat>> = _cheats.asStateFlow()

  // Set of actively held keys (input contract)
  private val _activeKeys = MutableStateFlow<Set<GamepadKey>>(emptySet())
  val activeKeys: StateFlow<Set<GamepadKey>> = _activeKeys.asStateFlow()

  // Memory Freeze Set
  private val frozenAddresses = mutableSetOf<String>()

  private var loopJob: Job? = null
  private var forceNextShiny = false
  private var speedMultiplier = 1.0f

  init {
    startEngineLoop()
  }

  fun forceShinyNextEncounter() {
    forceNextShiny = true
  }

  fun setSpeedMultiplier(multiplier: Float) {
    speedMultiplier = multiplier.coerceIn(0.25f, 8.0f)
  }

  fun restoreSnapshot(snapshot: GameStateSnapshot) {
    _gameState.value = snapshot
    rawMemoryWrite("0x02024284", snapshot.playerX)
    rawMemoryWrite("0x02024286", snapshot.playerY)
    rawMemoryWrite("0x0202402C", snapshot.playerHp)
    rawMemoryWrite("0x02024090", snapshot.coins)
  }

  fun stepSingleFrame() {
    val current = _gameState.value
    val newFrame = current.frameNumber + 1
    applyActiveCheats()
    processContinuousInputPhysics()
    val detections = generateVisionDetections(current)
    val watchEntries = generateMemoryWatchList(current)
    _gameState.update {
      it.copy(
        frameNumber = newFrame,
        detections = detections,
        memoryWatch = watchEntries
      )
    }
  }

  // --------------------------------------------------------------------------
  // INPUT CONTRACT: KeyDown, KeyUp, ReleaseAll, Analog, Touch
  // --------------------------------------------------------------------------

  fun onKeyDown(key: GamepadKey) {
    if (_gameState.value.isPaused) return
    _activeKeys.update { it + key }
    dispatchInputState()
  }

  fun onKeyUp(key: GamepadKey) {
    _activeKeys.update { it - key }
    dispatchInputState()
  }

  fun releaseAllKeys() {
    _activeKeys.value = emptySet()
    dispatchInputState()
  }

  fun onCirclePad(dx: Float, dy: Float) {
    if (_gameState.value.isPaused) return
    _gameState.update { it.copy(circlePadOffset = Pair(dx.coerceIn(-1f, 1f), dy.coerceIn(-1f, 1f))) }
  }

  fun onTouchDown(xNorm: Float, yNorm: Float) {
    if (_gameState.value.isPaused) return
    val point = TouchPoint(xNorm.coerceIn(0f, 1f), yNorm.coerceIn(0f, 1f), isDown = true)
    _gameState.update { it.copy(touchPoint = point) }

    // If in battle on 3DS bottom screen, evaluate touch zones
    handleTouchInteraction(xNorm, yNorm)
  }

  fun onTouchUp() {
    _gameState.update { it.copy(touchPoint = null) }
  }

  private fun handleTouchInteraction(x: Float, y: Float) {
    val state = _gameState.value
    if (state.isInBattle) {
      // 3DS 2x2 Battle Grid on bottom screen:
      // Top-Left: FIGHT (A)
      // Top-Right: BAG (Items)
      // Bottom-Left: POKEMON (Switch)
      // Bottom-Right: RUN (B)
      if (x < 0.5f && y < 0.5f) {
        // FIGHT
        onKeyDown(GamepadKey.A)
        scope.launch { delay(60); onKeyUp(GamepadKey.A) }
      } else if (x >= 0.5f && y >= 0.5f) {
        // RUN
        onKeyDown(GamepadKey.B)
        scope.launch { delay(60); onKeyUp(GamepadKey.B) }
      } else if (x >= 0.5f && y < 0.5f) {
        _gameState.update { it.copy(dialogText = "Opened Bag: 15x Poké Balls, 3x Potions available.") }
      } else {
        _gameState.update { it.copy(dialogText = "Party: 1. Starter (Lv ${state.playerLevel}), 2. Pidgey (Lv 3).") }
      }
    } else {
      // Overworld touch interaction
      if (x in 0.35f..0.65f && y in 0.35f..0.65f) {
        // Center tap interacts with object / dialog
        onKeyDown(GamepadKey.A)
        scope.launch { delay(60); onKeyUp(GamepadKey.A) }
      }
    }
  }

  private fun dispatchInputState() {
    val keys = _activeKeys.value
    val bufferHex = computeKeyInputBufferHex(keys)
    val lastName = if (keys.isNotEmpty()) keys.joinToString("+") { it.name } else "IDLE"

    _gameState.update {
      it.copy(
        pressedKeys = keys,
        lastInputKey = lastName,
        inputBufferHex = bufferHex
      )
    }
    _memoryMap.update { current ->
      current.toMutableMap().apply {
        put("0x03004020", bufferHex.removePrefix("0x").toIntOrNull(16) ?: 0)
      }
    }
  }

  /**
   * Computes authentic GBA REG_KEYINPUT active-low bitfield
   * Bit 0: A, Bit 1: B, Bit 2: Select, Bit 3: Start,
   * Bit 4: Right, Bit 5: Left, Bit 6: Up, Bit 7: Down, Bit 8: R, Bit 9: L
   */
  private fun computeKeyInputBufferHex(keys: Set<GamepadKey>): String {
    var bitmask = 0x03FF
    if (keys.contains(GamepadKey.A)) bitmask = bitmask and (1 shl 0).inv()
    if (keys.contains(GamepadKey.B)) bitmask = bitmask and (1 shl 1).inv()
    if (keys.contains(GamepadKey.SELECT)) bitmask = bitmask and (1 shl 2).inv()
    if (keys.contains(GamepadKey.START)) bitmask = bitmask and (1 shl 3).inv()
    if (keys.contains(GamepadKey.RIGHT)) bitmask = bitmask and (1 shl 4).inv()
    if (keys.contains(GamepadKey.LEFT)) bitmask = bitmask and (1 shl 5).inv()
    if (keys.contains(GamepadKey.UP)) bitmask = bitmask and (1 shl 6).inv()
    if (keys.contains(GamepadKey.DOWN)) bitmask = bitmask and (1 shl 7).inv()
    if (keys.contains(GamepadKey.R) || keys.contains(GamepadKey.ZR)) bitmask = bitmask and (1 shl 8).inv()
    if (keys.contains(GamepadKey.L) || keys.contains(GamepadKey.ZL)) bitmask = bitmask and (1 shl 9).inv()
    return String.format("0x%04X", bitmask)
  }

  // --------------------------------------------------------------------------
  // PAUSE & INPUT OWNERSHIP COORDINATION
  // --------------------------------------------------------------------------

  fun setPaused(paused: Boolean) {
    if (paused) {
      releaseAllKeys()
    }
    _gameState.update {
      it.copy(
        isPaused = paused,
        inputOwner = if (paused) InputOwner.PAUSED else it.inputOwner
      )
    }
  }

  fun setInputOwner(owner: InputOwner) {
    _gameState.update { it.copy(inputOwner = owner) }
  }

  // --------------------------------------------------------------------------
  // SCENARIO & CONSOLE MANAGEMENT
  // --------------------------------------------------------------------------

  fun switchScenario(scenario: GameScenario) {
    releaseAllKeys()
    _gameState.update {
      it.copy(
        scenario = scenario,
        consoleMode = scenario.platform,
        playerX = 14,
        playerY = 16,
        playerHp = scenario.initialHp,
        playerMaxHp = scenario.maxHp,
        playerLevel = scenario.initialLevel,
        coins = scenario.initialCoins,
        zoneName = scenario.defaultZone,
        isInBattle = false,
        enemyName = null,
        dialogText = when (scenario) {
          GameScenario.POKEMON_RED -> "Oak: 'Wait! Don't go out! Wild Pokémon live in tall grass!'"
          GameScenario.POKEMON_EMERALD -> "Now tell me. Are you a boy or a girl?"
          GameScenario.ZELDA_MINISH -> "Link, head to the Picori Festival at the castle!"
          GameScenario.MARIO_ADVANCE -> "Press A to jump! Watch out for Goombas."
          GameScenario.POKEMON_SUN_3DS -> "Alola! Welcome to the tropical paradise of Melemele."
          GameScenario.ZELDA_OOT_3DS -> "Navi: 'Hey! Listen! Link, wake up!'"
        }
      )
    }
    rebuildMemoryWatch()
  }

  fun switchConsoleMode(mode: GameConsoleMode) {
    _gameState.update { it.copy(consoleMode = mode) }
  }

  // --------------------------------------------------------------------------
  // EMULATION TICK & PHYSICS ENGINE LOOP
  // --------------------------------------------------------------------------

  private fun startEngineLoop() {
    loopJob?.cancel()
    loopJob = scope.launch(Dispatchers.Default) {
      var frame = 0L
      var physicsCounter = 0
      while (isActive) {
        frame++
        delay((16L / speedMultiplier).toLong().coerceAtLeast(2L))

        // Halt frame progression if paused
        if (_gameState.value.isPaused) {
          continue
        }

        // Apply cheats if active
        applyActiveCheats()

        // Physics movement tick (every 4 frames ~15 FPS step rate when keys are held)
        physicsCounter++
        if (physicsCounter % 4 == 0) {
          processContinuousInputPhysics()
        }

        // Update real-time CV detections and memory state
        val current = _gameState.value
        val detections = generateVisionDetections(current)
        val watchEntries = generateMemoryWatchList(current)

        _gameState.update {
          it.copy(
            frameNumber = frame,
            detections = detections,
            memoryWatch = watchEntries,
            activeCheatsCount = _cheats.value.count { cheat -> cheat.isEnabled }
          )
        }
      }
    }
  }

  private fun processContinuousInputPhysics() {
    val keys = _activeKeys.value
    val circleOffset = _gameState.value.circlePadOffset
    if (keys.isEmpty() && circleOffset == Pair(0f, 0f)) return

    _gameState.update { state ->
      val stepMultiplier = if (_cheats.value.any { it.id == "c5" && it.isEnabled }) 2 else 1
      var newX = state.playerX
      var newY = state.playerY

      // Simultaneous Multi-Direction handling (supports diagonal movement!)
      var dx = 0
      var dy = 0

      if (keys.contains(GamepadKey.UP) || circleOffset.second < -0.3f) dy -= 1
      if (keys.contains(GamepadKey.DOWN) || circleOffset.second > 0.3f) dy += 1
      if (keys.contains(GamepadKey.LEFT) || circleOffset.first < -0.3f) dx -= 1
      if (keys.contains(GamepadKey.RIGHT) || circleOffset.first > 0.3f) dx += 1

      // Speed turbo if B is held (B-Dash!)
      val runMultiplier = if (keys.contains(GamepadKey.B)) 2 else 1
      val totalStep = stepMultiplier * runMultiplier

      newX = (newX + dx * totalStep).coerceIn(4, 36)
      newY = (newY + dy * totalStep).coerceIn(4, 36)

      // Random wild encounter chance when moving through grass in overworld
      var battle = state.isInBattle
      var enemy = state.enemyName
      var enemyHp = state.enemyHp
      var enemyMaxHp = state.enemyMaxHp
      var dialog = state.dialogText
      var isShiny = state.isEnemyShiny

      if (!battle && (dx != 0 || dy != 0)) {
        if (Random.nextInt(100) < 8) { // 8% encounter check
          battle = true
          val wildList = if (state.scenario == GameScenario.POKEMON_RED) {
            listOf("Pidgey", "Rattata", "Nidoran♂", "Pikachu", "Caterpie")
          } else {
            listOf("Poochyena", "Zigzagoon", "Wurmple", "Taillow", "Wingull")
          }
          val baseName = wildList.random()
          isShiny = forceNextShiny || (Random.nextInt(48) == 0)
          forceNextShiny = false

          enemy = if (isShiny) "Shiny $baseName ✨" else "Wild $baseName"
          enemyHp = 18
          enemyMaxHp = 18
          dialog = if (isShiny) {
            "✨ A wild SHINY $baseName appeared! Golden sparkles burst! ✨"
          } else {
            "A $enemy appeared! What will you do?"
          }
        }
      }

      // Single action clicks (A attacks in battle)
      var newCoins = state.coins
      var newExp = state.playerExp
      var newLevel = state.playerLevel
      if (keys.contains(GamepadKey.A) && battle) {
        val newEnemyHp = (enemyHp - 7).coerceAtLeast(0)
        if (newEnemyHp == 0) {
          battle = false
          enemy = null
          isShiny = false
          newCoins += 120
          newExp += 74
          if (newExp >= 100) {
            newLevel += 1
            newExp -= 100
            dialog = "Enemy defeated! Gained 74 EXP & $120. Leveled up to Lv $newLevel!"
          } else {
            dialog = "Enemy defeated! Gained 74 EXP and $120."
          }
        } else {
          enemyHp = newEnemyHp
          dialog = "Direct hit! Enemy HP down to $newEnemyHp."
        }
      }

      state.copy(
        playerX = newX,
        playerY = newY,
        coins = newCoins,
        playerExp = newExp,
        playerLevel = newLevel,
        isInBattle = battle,
        enemyName = enemy,
        enemyHp = enemyHp,
        enemyMaxHp = enemyMaxHp,
        isEnemyShiny = isShiny,
        shinySparkles = isShiny,
        dialogText = dialog
      )
    }

    // Reflect player coordinates and stats in live emulated RAM
    rawMemoryWrite("0x02024284", _gameState.value.playerX)
    rawMemoryWrite("0x02024286", _gameState.value.playerY)
    rawMemoryWrite("0x02024030", _gameState.value.coins)
    rawMemoryWrite("0x02024038", _gameState.value.playerLevel)
  }

  // --------------------------------------------------------------------------
  // UNIFIED MEMORY GATEKEEPER & CHEATS
  // --------------------------------------------------------------------------

  fun readMemory(addressHex: String): Int {
    return _memoryMap.value[addressHex] ?: 0
  }

  /**
   * Safe Memory Poke with Strict Permission Check
   */
  fun pokeMemory(addressHex: String, valueHex: String, allowCheats: Boolean): Result<Unit> {
    if (!allowCheats) {
      return Result.failure(SecurityException("Memory write blocked: 'Memory Cheats Allowed' is disabled in AI Settings."))
    }
    val cleanVal = valueHex.removePrefix("0x").toIntOrNull(16) ?: 0
    rawMemoryWrite(addressHex, cleanVal)

    // Sync state properties if primary registers are modified
    when (addressHex) {
      "0x0202402C" -> _gameState.update { it.copy(playerHp = cleanVal.coerceIn(0, it.playerMaxHp)) }
      "0x02024090" -> _gameState.update { it.copy(coins = cleanVal) }
      "0x02024284" -> _gameState.update { it.copy(playerX = cleanVal.coerceIn(4, 36)) }
      "0x02024286" -> _gameState.update { it.copy(playerY = cleanVal.coerceIn(4, 36)) }
    }
    return Result.success(Unit)
  }

  private fun rawMemoryWrite(addressHex: String, decimalVal: Int) {
    if (frozenAddresses.contains(addressHex)) return
    _memoryMap.update { current ->
      current.toMutableMap().apply { put(addressHex, decimalVal) }
    }
  }

  fun toggleCheat(cheatId: String, allowCheats: Boolean): Result<Boolean> {
    if (!allowCheats) {
      return Result.failure(SecurityException("Cheat activation rejected: 'Memory Cheats Allowed' is disabled in AI Settings."))
    }
    var newState = false
    _cheats.update { list ->
      list.map { c ->
        if (c.id == cheatId) {
          newState = !c.isEnabled
          if (newState) {
            rawMemoryWrite(c.addressHex, c.activeValueHex.removePrefix("0x").toIntOrNull(16) ?: 0)
          }
          c.copy(isEnabled = newState)
        } else c
      }
    }
    return Result.success(newState)
  }

  private fun applyActiveCheats() {
    val enabledCheats = _cheats.value.filter { it.isEnabled }
    for (cheat in enabledCheats) {
      when (cheat.id) {
        "c1" -> { // God Mode
          _gameState.update { it.copy(playerHp = it.playerMaxHp) }
          rawMemoryWrite("0x0202402C", _gameState.value.playerMaxHp)
        }
        "c3" -> { // Max Money
          _gameState.update { it.copy(coins = 999999) }
          rawMemoryWrite("0x02024090", 999999)
        }
        "c4" -> { // One Hit KO
          if (_gameState.value.isInBattle && _gameState.value.enemyHp > 1) {
            _gameState.update { it.copy(enemyHp = 1) }
          }
        }
      }
    }
  }

  fun toggleFreezeMemory(addressHex: String) {
    if (frozenAddresses.contains(addressHex)) {
      frozenAddresses.remove(addressHex)
    } else {
      frozenAddresses.add(addressHex)
    }
  }

  private fun rebuildMemoryWatch() {
    val current = _gameState.value
    _memoryMap.update {
      mapOf(
        "0x02024284" to current.playerX,
        "0x02024286" to current.playerY,
        "0x0202402C" to current.playerHp,
        "0x0202402E" to current.playerMaxHp,
        "0x02024090" to current.coins,
        "0x02024036" to current.playerLevel,
        "0x020386E0" to if (current.isInBattle) 1 else 0,
        "0x03004020" to 0x03FF
      )
    }
  }

  private fun generateVisionDetections(state: GameStateSnapshot): List<VisionDetection> {
    val list = mutableListOf<VisionDetection>()
    val normPxX = (state.playerX / 40.0f).coerceIn(0.1f, 0.85f)
    val normPxY = (state.playerY / 40.0f).coerceIn(0.15f, 0.75f)

    list.add(
      VisionDetection(
        id = "det_player",
        label = "Player (Tile ${state.playerX}, ${state.playerY})",
        category = VisionCategory.PLAYER,
        xNorm = normPxX,
        yNorm = normPxY,
        widthNorm = 0.14f,
        heightNorm = 0.18f,
        confidence = 0.992f,
        extraInfo = "HP: ${state.playerHp}/${state.playerMaxHp} | Coins: \$${state.coins}"
      )
    )

    if (state.isInBattle && state.enemyName != null) {
      list.add(
        VisionDetection(
          id = "det_enemy",
          label = "${state.enemyName} (HP ${state.enemyHp}/${state.enemyMaxHp})",
          category = VisionCategory.ENEMY,
          xNorm = 0.62f,
          yNorm = 0.18f,
          widthNorm = 0.28f,
          heightNorm = 0.32f,
          confidence = 0.985f,
          extraInfo = "Battle Target"
        )
      )
      list.add(
        VisionDetection(
          id = "det_hp_bar",
          label = "HP Gauge [${if (state.enemyMaxHp > 0) ((state.enemyHp.toFloat() / state.enemyMaxHp) * 100).toInt() else 0}%]",
          category = VisionCategory.HP_BAR,
          xNorm = 0.58f,
          yNorm = 0.12f,
          widthNorm = 0.35f,
          heightNorm = 0.06f,
          confidence = 0.978f
        )
      )
    } else {
      list.add(
        VisionDetection(
          id = "det_npc",
          label = "Professor Oak / Birch (NPC)",
          category = VisionCategory.NPC,
          xNorm = 0.72f,
          yNorm = 0.38f,
          widthNorm = 0.12f,
          heightNorm = 0.16f,
          confidence = 0.965f,
          extraInfo = "Dialogue Source"
        )
      )
    }

    if (state.dialogText.isNotBlank()) {
      list.add(
        VisionDetection(
          id = "det_dialog",
          label = "OCR Text Box [${state.dialogText.take(20)}...]",
          category = VisionCategory.DIALOG,
          xNorm = 0.05f,
          yNorm = 0.62f,
          widthNorm = 0.90f,
          heightNorm = 0.32f,
          confidence = 0.994f,
          extraInfo = "Active Prompt"
        )
      )
    }

    return list
  }

  private fun generateMemoryWatchList(state: GameStateSnapshot): List<MemoryWatchEntry> {
    val map = _memoryMap.value
    return listOf(
      MemoryWatchEntry("0x02024284", "PLAYER_X", String.format("0x%04X", map["0x02024284"] ?: state.playerX), map["0x02024284"] ?: state.playerX, frozenAddresses.contains("0x02024284"), "Horizontal tile map coordinate"),
      MemoryWatchEntry("0x02024286", "PLAYER_Y", String.format("0x%04X", map["0x02024286"] ?: state.playerY), map["0x02024286"] ?: state.playerY, frozenAddresses.contains("0x02024286"), "Vertical tile map coordinate"),
      MemoryWatchEntry("0x0202402C", "PLAYER_HP", String.format("0x%04X", map["0x0202402C"] ?: state.playerHp), map["0x0202402C"] ?: state.playerHp, frozenAddresses.contains("0x0202402C"), "Current party leader Hit Points"),
      MemoryWatchEntry("0x0202402E", "PLAYER_MAX_HP", String.format("0x%04X", map["0x0202402E"] ?: state.playerMaxHp), map["0x0202402E"] ?: state.playerMaxHp, frozenAddresses.contains("0x0202402E"), "Max base HP capacity"),
      MemoryWatchEntry("0x02024090", "MONEY_POCKET", String.format("0x%08X", map["0x02024090"] ?: state.coins), map["0x02024090"] ?: state.coins, frozenAddresses.contains("0x02024090"), "Current wallet money counter"),
      MemoryWatchEntry("0x02024036", "POKEMON_LEVEL", String.format("0x%02X", map["0x02024036"] ?: state.playerLevel), map["0x02024036"] ?: state.playerLevel, frozenAddresses.contains("0x02024036"), "Starter creature level"),
      MemoryWatchEntry("0x020386E0", "BATTLE_FLAG", String.format("0x%02X", map["0x020386E0"] ?: if (state.isInBattle) 1 else 0), map["0x020386E0"] ?: if (state.isInBattle) 1 else 0, frozenAddresses.contains("0x020386E0"), "0 = Overworld, 1 = Battle screen"),
      MemoryWatchEntry("0x03004020", "KEYINPUT_REG", computeKeyInputBufferHex(_activeKeys.value), map["0x03004020"] ?: 0x03FF, false, "Live DMA 10-bit active-low button register")
    )
  }
}
