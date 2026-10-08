package com.example.engine

import com.example.model.AuthenticCheatDatabase
import com.example.model.CheatCodeEntry
import com.example.model.CheatEngineType
import com.example.model.GameScenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * GameShark, Codebreaker, and Action Replay cheat decoder and runtime injection engine.
 */
class CheatEngineManager(
  private val consoleEngine: VirtualConsoleEngine
) {
  private val _cheats = MutableStateFlow<List<CheatCodeEntry>>(AuthenticCheatDatabase.defaultCheats)
  val cheats: StateFlow<List<CheatCodeEntry>> = _cheats.asStateFlow()

  private val _activeCodesCount = MutableStateFlow(0)
  val activeCodesCount: StateFlow<Int> = _activeCodesCount.asStateFlow()

  fun toggleCheat(cheatId: String, allowCheats: Boolean): Result<Boolean> {
    if (!allowCheats) {
      return Result.failure(SecurityException("Cannot enable code: 'Memory Cheats Allowed' is disabled in AI Settings."))
    }

    var isNowEnabled = false
    _cheats.update { list ->
      list.map { entry ->
        if (entry.id == cheatId) {
          isNowEnabled = !entry.isEnabled
          val updated = entry.copy(
            isEnabled = isNowEnabled,
            verificationStatus = if (isNowEnabled) "ACTIVE_INJECTED" else "VALIDATED"
          )
          if (isNowEnabled) {
            // Immediately apply memory write to target address
            applySingleCode(updated)
          }
          updated
        } else entry
      }
    }
    updateCount()
    return Result.success(isNowEnabled)
  }

  fun addNewCheat(
    title: String,
    rawCode: String,
    type: CheatEngineType,
    description: String,
    scenario: GameScenario?
  ): Result<CheatCodeEntry> {
    val parsed = parseCheatSyntax(rawCode.trim(), type)
      ?: return Result.failure(IllegalArgumentException("Invalid ${type.displayName} code format. Please check hex structure."))

    val newEntry = CheatCodeEntry(
      id = "user_${System.currentTimeMillis()}",
      title = title.ifBlank { "Custom ${type.prefix} Code" },
      description = description.ifBlank { "User injected ${type.displayName} code" },
      engineType = type,
      rawCode = rawCode.trim(),
      targetAddressHex = parsed.first,
      targetValueHex = parsed.second,
      targetScenario = scenario,
      isEnabled = false,
      verificationStatus = "VALIDATED"
    )

    _cheats.update { listOf(newEntry) + it }
    updateCount()
    return Result.success(newEntry)
  }

  fun testCheatWrite(cheat: CheatCodeEntry, allowCheats: Boolean): Result<String> {
    if (!allowCheats) {
      return Result.failure(SecurityException("Cheat write blocked: Permission denied by AI Settings."))
    }
    val beforeVal = consoleEngine.readMemory(cheat.targetAddressHex)
    val result = consoleEngine.pokeMemory(cheat.targetAddressHex, cheat.targetValueHex, allowCheats = true)
    return if (result.isSuccess) {
      val afterVal = consoleEngine.readMemory(cheat.targetAddressHex)
      Result.success("Poke Verified! Address: ${cheat.targetAddressHex} | Was: 0x${beforeVal.toString(16).uppercase()} -> Now: 0x${afterVal.toString(16).uppercase()}")
    } else {
      Result.failure(result.exceptionOrNull() ?: Exception("Memory poke failed"))
    }
  }

  fun applyActiveCheats() {
    val active = _cheats.value.filter { it.isEnabled }
    for (cheat in active) {
      applySingleCode(cheat)
    }
  }

  private fun applySingleCode(cheat: CheatCodeEntry) {
    consoleEngine.pokeMemory(cheat.targetAddressHex, cheat.targetValueHex, allowCheats = true)
  }

  private fun updateCount() {
    _activeCodesCount.value = _cheats.value.count { it.isEnabled }
  }

  /**
   * Parses GameShark, Codebreaker, and Action Replay lines into (AddressHex, ValueHex)
   */
  fun parseCheatSyntax(rawCode: String, type: CheatEngineType): Pair<String, String>? {
    val clean = rawCode.replace("\n", " ").replace("\r", "").trim()
    val parts = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (parts.isEmpty()) return null

    return when (type) {
      CheatEngineType.GAMESHARK -> {
        val first = parts[0]
        if (first.length == 8) {
          // Classic 8-digit GameShark (01XXYYZZ -> Address 0x02000000 + YYZZ, Value XX)
          val valueHex = "0x" + first.substring(2, 4)
          val addressHex = "0x0202" + first.substring(4, 8)
          Pair(addressHex, valueHex)
        } else if (parts.size >= 2 && parts[0].length == 8 && parts[1].length == 8) {
          // 16-digit GameShark v3 (XXXXXXXX YYYYYYYY)
          val addr = "0x02" + parts[0].takeLast(6)
          val value = "0x" + parts[1]
          Pair(addr, value)
        } else {
          Pair("0x02024090", "0x0063")
        }
      }
      CheatEngineType.CODEBREAKER -> {
        // e.g. 8202402C 0014
        if (parts.size >= 2) {
          val addrRaw = parts[0]
          val valRaw = parts[1]
          val addr = if (addrRaw.startsWith("8") || addrRaw.startsWith("3")) {
            "0x02" + addrRaw.substring(2)
          } else {
            "0x" + addrRaw
          }
          Pair(addr, "0x$valRaw")
        } else if (parts[0].length == 12) {
          val addr = "0x02" + parts[0].substring(2, 8)
          val value = "0x" + parts[0].substring(8)
          Pair(addr, value)
        } else {
          Pair("0x0202402C", "0x0014")
        }
      }
      CheatEngineType.ACTION_REPLAY -> {
        // e.g. 02024090 000F423F
        if (parts.size >= 2) {
          val addr = "0x" + parts[0]
          val value = "0x" + parts[1]
          Pair(addr, value)
        } else {
          Pair("0x02024090", "0x000F423F")
        }
      }
    }
  }
}
