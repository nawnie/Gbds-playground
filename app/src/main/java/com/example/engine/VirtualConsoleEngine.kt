package com.example.engine

import com.example.model.GameCheat
import com.example.model.GameConsoleMode
import com.example.model.GamepadKey
import com.example.model.GameScenario
import com.example.model.GameStateSnapshot
import com.example.model.MemoryWatchEntry
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
 * Emulated Virtual Console Engine for GBA and 3DS systems
 * Features screen simulation, live RAM memory mapping, CV analysis overlays, and input buffers.
 */
class VirtualConsoleEngine(
  private val scope: CoroutineScope
) {
  private val _gameState = MutableStateFlow(
    GameStateSnapshot(
      scenario = GameScenario.POKEMON_RED,
      consoleMode = GameConsoleMode.GBA,
      zoneName = "Pallet Town - Route 1",
      dialogText = "Now tell me. Are you a boy? Or are you a girl?"
    )
  )
  val gameState: StateFlow<GameStateSnapshot> = _gameState.asStateFlow()

  // Real-time Memory Watch Map
  private val _memoryMap = MutableStateFlow<Map<String, Int>>(
    mapOf(
      "0x02024284" to 12,    // Player X
      "0x02024286" to 18,    // Player Y
      "0x0202402C" to 22,    // Player HP
      "0x0202402E" to 22,    // Player Max HP
      "0x02024090" to 3000,  // Money / Coins
      "0x02024036" to 5,     // Player Level
      "0x020386E0" to 0,     // Battle State Flag (0 = Field, 1 = Battle)
      "0x03004020" to 0x0000 // Key Input Buffer
    )
  )

  // Active Cheat Switches
  private val _cheats = MutableStateFlow<List<GameCheat>>(
    listOf(
      GameCheat("c1", "Infinite HP / God Mode", "Locks Player HP to Max HP permanently", "0x0202402C", "0x0016", false, "STATS"),
      GameCheat("c2", "Walk Through Walls (Ghost Clip)", "Disables tile collision checks in RAM", "0x020370E4", "0x0001", false, "MOVEMENT"),
      GameCheat("c3", "Max Money ($999,999)", "Pokes money offset to maximum value", "0x02024090", "0x000F423F", false, "RESOURCES"),
      GameCheat("c4", "One-Hit KO in Battles", "Reduces opponent HP to 0 on attack", "0x02024040", "0x0000", false, "BATTLE"),
      GameCheat("c5", "4x Turbo Movement Speed", "Overclocks step cycle speed in RAM", "0x02038010", "0x0004", false, "MOVEMENT"),
      GameCheat("c6", "Infinite Master Balls", "Injects 99 Master Balls into Bag offset", "0x02025800", "0x0063", false, "RESOURCES")
    )
  )
  val cheats: StateFlow<List<GameCheat>> = _cheats.asStateFlow()

  // Memory Freeze Set
  private val frozenAddresses = mutableSetOf<String>()

  private var loopJob: Job? = null

  init {
    startEngineLoop()
  }

  fun switchScenario(scenario: GameScenario) {
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

  private fun startEngineLoop() {
    loopJob?.cancel()
    loopJob = scope.launch(Dispatchers.Default) {
      var frame = 0L
      while (isActive) {
        frame++
        delay(16) // ~60 FPS update cycle

        // Apply any active memory cheats
        applyActiveCheats()

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

  private fun applyActiveCheats() {
    val enabledCheats = _cheats.value.filter { it.isEnabled }
    for (cheat in enabledCheats) {
      when (cheat.id) {
        "c1" -> { // God Mode
          _gameState.update { it.copy(playerHp = it.playerMaxHp) }
          pokeMemory("0x0202402C", String.format("0x%04X", _gameState.value.playerMaxHp))
        }
        "c3" -> { // Max Money
          _gameState.update { it.copy(coins = 999999) }
          pokeMemory("0x02024090", "0x000F423F")
        }
        "c4" -> { // One Hit KO
          if (_gameState.value.isInBattle && _gameState.value.enemyHp > 1) {
            _gameState.update { it.copy(enemyHp = 1) }
          }
        }
      }
    }
  }

  fun sendInput(key: GamepadKey) {
    val inputHex = when (key) {
      GamepadKey.UP -> "0x0040"
      GamepadKey.DOWN -> "0x0080"
      GamepadKey.LEFT -> "0x0020"
      GamepadKey.RIGHT -> "0x0010"
      GamepadKey.A -> "0x0001"
      GamepadKey.B -> "0x0002"
      GamepadKey.X -> "0x0100"
      GamepadKey.Y -> "0x0200"
      GamepadKey.L -> "0x0200"
      GamepadKey.R -> "0x0100"
      GamepadKey.START -> "0x0008"
      GamepadKey.SELECT -> "0x0004"
      GamepadKey.MENU -> "0x0000"
    }

    _gameState.update { state ->
      val stepMultiplier = if (_cheats.value.any { it.id == "c5" && it.isEnabled }) 2 else 1
      var newX = state.playerX
      var newY = state.playerY

      when (key) {
        GamepadKey.UP -> newY = (newY - stepMultiplier).coerceAtLeast(4)
        GamepadKey.DOWN -> newY = (newY + stepMultiplier).coerceAtMost(36)
        GamepadKey.LEFT -> newX = (newX - stepMultiplier).coerceAtLeast(4)
        GamepadKey.RIGHT -> newX = (newX + stepMultiplier).coerceAtMost(36)
        GamepadKey.A -> {
          // Progress dialogue or attack in battle
          if (state.isInBattle) {
            val newEnemyHp = (state.enemyHp - 8).coerceAtLeast(0)
            if (newEnemyHp == 0) {
              return@update state.copy(
                isInBattle = false,
                enemyName = null,
                enemyHp = 0,
                dialogText = "Enemy defeated! Gained 74 EXP and $120.",
                coins = state.coins + 120,
                lastInputKey = key.name,
                inputBufferHex = inputHex
              )
            } else {
              return@update state.copy(
                enemyHp = newEnemyHp,
                dialogText = "Critical Hit! ${state.enemyName} HP down to $newEnemyHp.",
                lastInputKey = key.name,
                inputBufferHex = inputHex
              )
            }
          }
        }
        GamepadKey.B -> {
          // Cancel / Run away
          if (state.isInBattle) {
            return@update state.copy(
              isInBattle = false,
              enemyName = null,
              dialogText = "Got away safely!",
              lastInputKey = key.name,
              inputBufferHex = inputHex
            )
          }
        }
        else -> Unit
      }

      // Check random wild encounter when moving in tall grass
      var battle = state.isInBattle
      var enemy = state.enemyName
      var enemyHp = state.enemyHp
      var enemyMaxHp = state.enemyMaxHp
      var dialog = state.dialogText

      if (!battle && (key == GamepadKey.UP || key == GamepadKey.DOWN || key == GamepadKey.LEFT || key == GamepadKey.RIGHT)) {
        if (Random.nextInt(100) < 12) { // 12% encounter chance on step
          battle = true
          val wildList = if (state.scenario == GameScenario.POKEMON_RED) {
            listOf("Wild Pidgey", "Wild Rattata", "Wild Nidoran♂", "Wild Pikachu", "Wild Caterpie")
          } else {
            listOf("Wild Poochyena", "Wild Zigzagoon", "Wild Wurmple", "Wild Taillow", "Wild Wingull")
          }
          enemy = wildList.random()
          enemyHp = 18
          enemyMaxHp = 18
          dialog = "A $enemy appeared! What will you do?"
        }
      }

      state.copy(
        playerX = newX,
        playerY = newY,
        isInBattle = battle,
        enemyName = enemy,
        enemyHp = enemyHp,
        enemyMaxHp = enemyMaxHp,
        dialogText = dialog,
        lastInputKey = key.name,
        inputBufferHex = inputHex
      )
    }

    // Update RAM
    pokeMemory("0x02024284", String.format("0x%04X", _gameState.value.playerX))
    pokeMemory("0x02024286", String.format("0x%04X", _gameState.value.playerY))
    pokeMemory("0x03004020", inputHex)
  }

  fun pokeMemory(addressHex: String, valueHex: String) {
    val cleanVal = valueHex.removePrefix("0x").toIntOrNull(16) ?: 0
    _memoryMap.update { current ->
      current.toMutableMap().apply {
        put(addressHex, cleanVal)
      }
    }
  }

  fun toggleCheat(cheatId: String) {
    _cheats.update { list ->
      list.map { c ->
        if (c.id == cheatId) {
          val newState = !c.isEnabled
          if (newState) {
            pokeMemory(c.addressHex, c.activeValueHex)
          }
          c.copy(isEnabled = newState)
        } else c
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
        "0x03004020" to 0x0000
      )
    }
  }

  private fun generateVisionDetections(state: GameStateSnapshot): List<VisionDetection> {
    val list = mutableListOf<VisionDetection>()

    // Player bounding box (normalized on screen)
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
        extraInfo = "Facing South | HP: ${state.playerHp}/${state.playerMaxHp}"
      )
    )

    // NPC / Enemy bounding box
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
          extraInfo = "Battle Active"
        )
      )
      // HP Gauge detection
      list.add(
        VisionDetection(
          id = "det_hp_bar",
          label = "HP Bar [${((state.enemyHp.toFloat() / state.enemyMaxHp) * 100).toInt()}%]",
          category = VisionCategory.HP_BAR,
          xNorm = 0.58f,
          yNorm = 0.12f,
          widthNorm = 0.35f,
          heightNorm = 0.05f,
          confidence = 0.995f,
          extraInfo = "Value: ${state.enemyHp}"
        )
      )
    } else {
      // NPC in town
      list.add(
        VisionDetection(
          id = "det_npc_1",
          label = "Prof. Birch / Town NPC",
          category = VisionCategory.NPC,
          xNorm = 0.70f,
          yNorm = 0.45f,
          widthNorm = 0.12f,
          heightNorm = 0.16f,
          confidence = 0.967f,
          extraInfo = "Quest Giver"
        )
      )
      // Next Path Waypoint
      list.add(
        VisionDetection(
          id = "det_waypoint",
          label = "Next Waypoint: Route 101 North",
          category = VisionCategory.WAYPOINT,
          xNorm = 0.48f,
          yNorm = 0.10f,
          widthNorm = 0.16f,
          heightNorm = 0.08f,
          confidence = 0.941f,
          extraInfo = "Optimal Path"
        )
      )
    }

    // Text Dialogue Box
    if (state.dialogText.isNotBlank()) {
      list.add(
        VisionDetection(
          id = "det_dialog",
          label = "OCR Text: \"${state.dialogText.take(24)}...\"",
          category = VisionCategory.DIALOG,
          xNorm = 0.08f,
          yNorm = 0.72f,
          widthNorm = 0.84f,
          heightNorm = 0.22f,
          confidence = 0.989f,
          extraInfo = "Text Dialog Box"
        )
      )
    }

    return list
  }

  private fun generateMemoryWatchList(state: GameStateSnapshot): List<MemoryWatchEntry> {
    val map = _memoryMap.value
    return listOf(
      MemoryWatchEntry("0x02024284", "Player_X_Pos", String.format("0x%04X", state.playerX), state.playerX, frozenAddresses.contains("0x02024284"), "Tile coordinate X"),
      MemoryWatchEntry("0x02024286", "Player_Y_Pos", String.format("0x%04X", state.playerY), state.playerY, frozenAddresses.contains("0x02024286"), "Tile coordinate Y"),
      MemoryWatchEntry("0x0202402C", "Player_HP", String.format("0x%04X", state.playerHp), state.playerHp, frozenAddresses.contains("0x0202402C"), "Current active hit points"),
      MemoryWatchEntry("0x0202402E", "Player_Max_HP", String.format("0x%04X", state.playerMaxHp), state.playerMaxHp, frozenAddresses.contains("0x0202402E"), "Max allowable hit points"),
      MemoryWatchEntry("0x02024090", "Money_Coins", String.format("0x%08X", state.coins), state.coins, frozenAddresses.contains("0x02024090"), "Pocket funds"),
      MemoryWatchEntry("0x02024036", "Level_Lead", String.format("0x%02X", state.playerLevel), state.playerLevel, frozenAddresses.contains("0x02024036"), "Lead party member level"),
      MemoryWatchEntry("0x020386E0", "Battle_State_Flag", String.format("0x%02X", if (state.isInBattle) 1 else 0), if (state.isInBattle) 1 else 0, false, "0=Overworld, 1=Combat"),
      MemoryWatchEntry("0x03004020", "Key_Input_Buffer", state.inputBufferHex, map["0x03004020"] ?: 0, false, "Direct DMA controller latch")
    )
  }
}
