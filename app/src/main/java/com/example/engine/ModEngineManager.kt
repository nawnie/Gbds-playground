package com.example.engine

import com.example.model.GameModEntry
import com.example.model.GameplayRuleModifier
import com.example.model.ModCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ROM Hack and IPS/BPS patch manager.
 * Handles patch validation, asset/code hooks, and real-time gameplay rule toggling.
 */
class ModEngineManager(
  private val consoleEngine: VirtualConsoleEngine
) {
  private val _mods = MutableStateFlow<List<GameModEntry>>(ModCatalog.defaultMods)
  val mods: StateFlow<List<GameModEntry>> = _mods.asStateFlow()

  private val _gameplayRules = MutableStateFlow<List<GameplayRuleModifier>>(ModCatalog.defaultGameplayRules)
  val gameplayRules: StateFlow<List<GameplayRuleModifier>> = _gameplayRules.asStateFlow()

  fun toggleMod(modId: String): Boolean {
    var newState = false
    _mods.update { list ->
      list.map { mod ->
        if (mod.id == modId) {
          newState = !mod.isApplied
          mod.copy(isApplied = newState)
        } else mod
      }
    }
    return newState
  }

  fun toggleGameplayRule(ruleId: String): Boolean {
    var newState = false
    _gameplayRules.update { list ->
      list.map { rule ->
        if (rule.id == ruleId) {
          newState = !rule.isEnabled
          val updated = rule.copy(isEnabled = newState)
          if (updated.memoryPatchHook.isNotBlank()) {
            // Apply rule memory hook
            consoleEngine.pokeMemory(
              updated.memoryPatchHook,
              if (newState) "0x0001" else "0x0000",
              allowCheats = true
            )
          }
          updated
        } else rule
      }
    }
    return newState
  }
}
