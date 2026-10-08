package com.example.engine

import com.example.model.EncounterRecord
import com.example.model.GamepadKey
import com.example.model.GameScenario
import com.example.model.GrindBotGoal
import com.example.model.GrindBotState
import com.example.model.GrindBotTelemetry
import com.example.model.InputOwner
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
 * Deterministic AI Grind Bot for Pokémon handheld game automation.
 * Features automated grass patrol, intelligent battle execution, health recovery,
 * and dedicated "Grind Until Shiny is Found" hunting with instant lock & capture alerts.
 */
class GrindBotEngine(
  private val consoleEngine: VirtualConsoleEngine,
  private val ttsManager: TtsManager,
  private val hapticManager: HapticFeedbackManager,
  private val scope: CoroutineScope
) {
  private val _telemetry = MutableStateFlow(GrindBotTelemetry())
  val telemetry: StateFlow<GrindBotTelemetry> = _telemetry.asStateFlow()

  private var botJob: Job? = null
  private var lastBattleSeen = false
  private var lastEnemyName: String? = null

  // --------------------------------------------------------------------------
  // CONTROL LIFECYCLE
  // --------------------------------------------------------------------------

  fun startBot(goal: GrindBotGoal = _telemetry.value.goal) {
    if (_telemetry.value.isEnabled) return

    _telemetry.update {
      it.copy(
        isEnabled = true,
        goal = goal,
        currentState = GrindBotState.PATROLLING_GRASS,
        statusMessage = "AI Grind Bot Active: Patrolling area for ${goal.displayName}..."
      )
    }

    consoleEngine.setInputOwner(InputOwner.AI_AGENT)
    ttsManager.speak("Grind bot started. Objective: ${goal.displayName}.")

    botJob?.cancel()
    botJob = scope.launch(Dispatchers.Default) {
      runGrindLoop()
    }
  }

  fun stopBot(reason: String = "User paused bot") {
    botJob?.cancel()
    botJob = null
    consoleEngine.releaseAllKeys()
    consoleEngine.setInputOwner(InputOwner.HUMAN)

    _telemetry.update {
      it.copy(
        isEnabled = false,
        currentState = GrindBotState.STOPPED,
        statusMessage = "AI Grind Bot Stopped ($reason)."
      )
    }
  }

  fun setGoal(goal: GrindBotGoal) {
    _telemetry.update { it.copy(goal = goal) }
    if (_telemetry.value.isEnabled) {
      ttsManager.speak("Goal updated to ${goal.displayName}.")
    }
  }

  fun updateSettings(
    targetLevel: Int = _telemetry.value.targetLevel,
    targetMoney: Int = _telemetry.value.targetMoney,
    pauseOnShiny: Boolean = _telemetry.value.pauseOnShiny,
    autoHealThreshold: Int = _telemetry.value.autoHealThresholdHp
  ) {
    _telemetry.update {
      it.copy(
        targetLevel = targetLevel,
        targetMoney = targetMoney,
        pauseOnShiny = pauseOnShiny,
        autoHealThresholdHp = autoHealThreshold
      )
    }
  }

  fun forceShinyEncounter() {
    // Debug hook to test shiny alert immediately
    consoleEngine.forceShinyNextEncounter()
  }

  // --------------------------------------------------------------------------
  // DETERMINISTIC AUTOMATION LOOP
  // --------------------------------------------------------------------------

  private suspend fun runGrindLoop() {
    var patrolStep = 0
    var moveDir = GamepadKey.LEFT

    while (scope.isActive && _telemetry.value.isEnabled) {
      val state = consoleEngine.gameState.value

      // If game is paused (e.g., menu opened), wait
      if (state.isPaused) {
        delay(250)
        continue
      }

      // Check if goal conditions are reached
      val currentTelemetry = _telemetry.value
      if (currentTelemetry.goal == GrindBotGoal.LEVEL_UP && state.playerLevel >= currentTelemetry.targetLevel) {
        ttsManager.speak("Target Level ${currentTelemetry.targetLevel} reached! Grind complete.")
        stopBot("Level Goal Reached (Lv ${state.playerLevel})")
        break
      }
      if (currentTelemetry.goal == GrindBotGoal.MONEY_FARM && state.coins >= currentTelemetry.targetMoney) {
        ttsManager.speak("Target prize money reached! Total: \$${state.coins}.")
        stopBot("Money Goal Reached (\$${state.coins})")
        break
      }

      // 1. IN BATTLE ROUTINE
      if (state.isInBattle) {
        if (!lastBattleSeen) {
          // New battle started!
          handleNewBattleEngagement(state)
          lastBattleSeen = true
          delay(400)
          continue
        }

        // Check if shiny was locked
        if (_telemetry.value.currentState == GrindBotState.SHINY_LOCKED) {
          // Bot is locked to preserve shiny! Do not attack.
          delay(500)
          continue
        }

        executeBattleTactics(state)
        delay(320)
        continue
      }

      // 2. POST BATTLE / OVERWORLD RECOVERY
      if (lastBattleSeen && !state.isInBattle) {
        // Battle just concluded!
        lastBattleSeen = false
        _telemetry.update {
          it.copy(
            currentState = GrindBotState.VICTORY_CLEAR,
            totalBattlesWon = it.totalBattlesWon + 1,
            statusMessage = "Battle Won! Clearing dialog and resuming area patrol."
          )
        }
        // Press A to clear victory dialogue
        consoleEngine.onKeyDown(GamepadKey.A)
        delay(80)
        consoleEngine.onKeyUp(GamepadKey.A)
        delay(300)

        // Health recovery check
        if (state.playerHp <= _telemetry.value.autoHealThresholdHp) {
          performAutoHeal()
        }

        _telemetry.update { it.copy(currentState = GrindBotState.PATROLLING_GRASS) }
        delay(200)
        continue
      }

      // 3. OVERWORLD GRASS PATROL
      _telemetry.update {
        it.copy(
          currentState = GrindBotState.PATROLLING_GRASS,
          patrolStepsCount = it.patrolStepsCount + 1,
          statusMessage = "Hunting wild encounters in ${state.zoneName}... (Encounters: ${it.totalEncounters})"
        )
      }

      // Walk back and forth in grass patch
      patrolStep++
      if (patrolStep % 4 == 0) {
        moveDir = if (moveDir == GamepadKey.LEFT) GamepadKey.RIGHT else GamepadKey.LEFT
      }

      // Step in direction
      consoleEngine.onKeyDown(moveDir)
      delay(160)
      consoleEngine.onKeyUp(moveDir)
      delay(80)
    }
  }

  // --------------------------------------------------------------------------
  // BATTLE MANAGEMENT & SHINY RECOGNITION
  // --------------------------------------------------------------------------

  private fun handleNewBattleEngagement(state: com.example.model.GameStateSnapshot) {
    val enemy = state.enemyName ?: "Wild Pokémon"
    val isShiny = state.isEnemyShiny
    val encounterNum = _telemetry.value.totalEncounters + 1

    val record = EncounterRecord(
      encounterNumber = encounterNum,
      pokemonName = enemy,
      level = state.scenario.initialLevel,
      isShiny = isShiny,
      actionTaken = if (isShiny) "SHINY LOCKED" else "ENGAGING BATTLE"
    )

    _telemetry.update {
      it.copy(
        totalEncounters = encounterNum,
        lastEncounterName = enemy,
        lastEncounterShiny = isShiny,
        recentEncounters = (listOf(record) + it.recentEncounters).take(30),
        currentState = if (isShiny) GrindBotState.SHINY_LOCKED else GrindBotState.BATTLE_ENGAGED,
        statusMessage = if (isShiny) "✨ SHINY FOUND: $enemy! Locking bot!" else "Encounter #$encounterNum: $enemy engaged."
      )
    }

    if (isShiny) {
      _telemetry.update {
        it.copy(
          totalShiniesFound = it.totalShiniesFound + 1,
          shinyEncounterNumber = encounterNum
        )
      }
      hapticManager.triggerShinyAlert()
      ttsManager.speak("Attention! A shiny $enemy appeared on encounter number $encounterNum! Pausing bot for capture!")

      if (_telemetry.value.pauseOnShiny) {
        // Halt and yield control to player so they don't lose the shiny!
        consoleEngine.releaseAllKeys()
        consoleEngine.setInputOwner(InputOwner.HUMAN)
      }
    }
  }

  private fun executeBattleTactics(state: com.example.model.GameStateSnapshot) {
    val currentGoal = _telemetry.value.goal

    // If catching goal or shiny catch routine
    if (currentGoal == GrindBotGoal.CATCH_ALL) {
      _telemetry.update {
        it.copy(
          currentState = GrindBotState.BATTLE_CATCHING,
          totalBallsThrown = it.totalBallsThrown + 1,
          statusMessage = "Throwing Poké Ball at ${state.enemyName}..."
        )
      }
      // Press A to throw ball / execute catch
      consoleEngine.onKeyDown(GamepadKey.A)
      scope.launch {
        delay(60)
        consoleEngine.onKeyUp(GamepadKey.A)
      }
      return
    }

    // Default: Execute best attack move
    _telemetry.update {
      it.copy(
        currentState = GrindBotState.BATTLE_FIGHTING,
        statusMessage = "Attacking ${state.enemyName} (Opponent HP: ${state.enemyHp}/${state.enemyMaxHp})..."
      )
    }

    // Select Fight (A) -> Select Move 1 (A)
    consoleEngine.onKeyDown(GamepadKey.A)
    scope.launch {
      delay(60)
      consoleEngine.onKeyUp(GamepadKey.A)
    }
  }

  private suspend fun performAutoHeal() {
    _telemetry.update {
      it.copy(
        currentState = GrindBotState.HEALING_TRIP,
        statusMessage = "HP Low! Consuming Potion from inventory to restore full health."
      )
    }
    ttsManager.speak("Low HP detected. Applying Potion.")
    delay(400)
    // Restore health in memory
    consoleEngine.pokeMemory("0x0202402C", "0x0014", allowCheats = true)
    delay(300)
  }
}
