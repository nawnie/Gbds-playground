package com.example.engine

import com.example.model.DevLogCategory
import com.example.model.DevLogMessage
import com.example.model.GameStateSnapshot
import com.example.model.IoRegisterEntry
import com.example.model.MemoryHexRow
import com.example.model.MemoryRegion
import com.example.model.OamSpriteEntry
import com.example.model.PaletteColorEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * Developer tools engine providing low-level hardware inspection:
 * Live RAM Hex viewer/editor, VRAM & palette visualizer, OAM sprite table,
 * CPU frame stepping & rewind buffer, hardware I/O registers, and event logging.
 */
class DevToolsEngine(
  private val consoleEngine: VirtualConsoleEngine
) {
  // Speed multiplier: 0.5x, 1x, 2x, 4x, 8x
  private val _speedMultiplier = MutableStateFlow(1.0f)
  val speedMultiplier: StateFlow<Float> = _speedMultiplier.asStateFlow()

  // Frame Rewind Buffer (stores last 60 frames)
  private val rewindBuffer = ArrayDeque<GameStateSnapshot>(60)

  // Real-time dev logs
  private val _devLogs = MutableStateFlow<List<DevLogMessage>>(
    listOf(
      DevLogMessage(category = DevLogCategory.SYSTEM, tag = "BIOS", message = "GBA ARM7TDMI 16.78 MHz CPU clock initialized."),
      DevLogMessage(category = DevLogCategory.MEMORY, tag = "DMA", message = "DMA3 memory channel bound to V-Blank handler."),
      DevLogMessage(category = DevLogCategory.GRIND_BOT, tag = "BOT", message = "Deterministic AI Grind engine ready for Pokémon automation."),
      DevLogMessage(category = DevLogCategory.CHEAT_HOOK, tag = "CHEATS", message = "GameShark & Codebreaker memory hooks verified."),
      DevLogMessage(category = DevLogCategory.MOD_LOADER, tag = "MODS", message = "IPS patcher and 60 FPS turbo engine hooked.")
    )
  )
  val devLogs: StateFlow<List<DevLogMessage>> = _devLogs.asStateFlow()

  fun log(category: DevLogCategory, tag: String, message: String, level: String = "INFO") {
    _devLogs.update { (listOf(DevLogMessage(category = category, tag = tag, message = message, level = level)) + it).take(100) }
  }

  fun clearLogs() {
    _devLogs.value = emptyList()
  }

  fun pushFrameSnapshot(snapshot: GameStateSnapshot) {
    if (rewindBuffer.size >= 60) {
      rewindBuffer.removeFirst()
    }
    rewindBuffer.addLast(snapshot)
  }

  fun rewindFrame(): Boolean {
    if (rewindBuffer.size > 1) {
      rewindBuffer.removeLast() // remove current
      val previous = rewindBuffer.removeLast()
      consoleEngine.restoreSnapshot(previous)
      log(DevLogCategory.SYSTEM, "REWIND", "Rewound 1 frame to Frame #${previous.frameNumber}")
      return true
    }
    return false
  }

  fun stepSingleFrame() {
    consoleEngine.stepSingleFrame()
    log(DevLogCategory.SYSTEM, "STEP", "Stepped execution forward by 1 frame")
  }

  fun stepMultipleFrames(count: Int) {
    repeat(count) {
      consoleEngine.stepSingleFrame()
    }
    log(DevLogCategory.SYSTEM, "STEP", "Stepped execution forward by $count frames")
  }

  fun setSpeedMultiplier(speed: Float) {
    _speedMultiplier.value = speed
    consoleEngine.setSpeedMultiplier(speed)
    log(DevLogCategory.SYSTEM, "SPEED", "Emulation speed set to ${speed}x")
  }

  // --------------------------------------------------------------------------
  // HEX VIEWER & MEMORY INSPECTOR
  // --------------------------------------------------------------------------

  fun generateHexRows(region: MemoryRegion, offsetRows: Int, rowCount: Int = 16): List<MemoryHexRow> {
    val baseInt = region.baseAddressHex.removePrefix("0x").toInt(16)
    val startAddr = baseInt + (offsetRows * 16)
    val list = mutableListOf<MemoryHexRow>()

    for (r in 0 until rowCount) {
      val rowAddr = startAddr + (r * 16)
      val bytes = mutableListOf<Int>()
      val chars = StringBuilder()

      for (b in 0 until 16) {
        val cellAddr = rowAddr + b
        // Fetch known values or deterministic hardware bytes
        val byteVal = getSimulatedMemoryByte(cellAddr)
        bytes.add(byteVal)
        val char = if (byteVal in 32..126) byteVal.toChar() else '.'
        chars.append(char)
      }

      list.add(
        MemoryHexRow(
          addressHex = String.format("0x%08X", rowAddr),
          addressInt = rowAddr,
          bytes = bytes,
          ascii = chars.toString()
        )
      )
    }
    return list
  }

  fun editByte(addressHex: String, byteVal: Int): Result<Unit> {
    val result = consoleEngine.pokeMemory(addressHex, String.format("0x%02X", byteVal), allowCheats = true)
    if (result.isSuccess) {
      log(DevLogCategory.MEMORY, "WRITE", "Poked byte at $addressHex = 0x${byteVal.toString(16).uppercase()}")
    }
    return result
  }

  private fun getSimulatedMemoryByte(address: Int): Int {
    val addrHex = String.format("0x%08X", address)
    val knownVal = consoleEngine.readMemory(addrHex)
    if (knownVal != 0) return knownVal and 0xFF

    // Deterministic pseudo-RAM pattern based on address hash
    val hash = (address xor (address ushr 8) xor (address ushr 16)) and 0xFF
    return hash
  }

  // --------------------------------------------------------------------------
  // HARDWARE I/O REGISTERS
  // --------------------------------------------------------------------------

  fun getIoRegisters(): List<IoRegisterEntry> {
    val state = consoleEngine.gameState.value
    return listOf(
      IoRegisterEntry(
        addressHex = "0x04000000",
        name = "DISPCNT",
        description = "LCD Display Control Register",
        valueHex = "0x0080",
        valueInt = 0x0080,
        bitfieldDetails = listOf("Mode 0 (Tiled)", "BG0 Enabled", "BG1 Enabled", "OBJ 1D Mapping: Yes", "Forced Blank: No")
      ),
      IoRegisterEntry(
        addressHex = "0x04000006",
        name = "VCOUNT",
        description = "Vertical Scanline Counter (0..227)",
        valueHex = String.format("0x%04X", (state.frameNumber % 228).toInt()),
        valueInt = (state.frameNumber % 228).toInt(),
        bitfieldDetails = listOf("Current Scanline: ${(state.frameNumber % 228).toInt()}", if ((state.frameNumber % 228) >= 160) "In V-Blank (Halted)" else "Active Draw Scanline")
      ),
      IoRegisterEntry(
        addressHex = "0x04000008",
        name = "BG0CNT",
        description = "Background 0 Control (Overworld Tiles)",
        valueHex = "0x1F08",
        valueInt = 0x1F08,
        bitfieldDetails = listOf("Priority: 0", "Screen Base: 31", "Char Base: 0", "Color Mode: 256 colors", "Screen Size: 32x32")
      ),
      IoRegisterEntry(
        addressHex = "0x04000130",
        name = "KEYINPUT",
        description = "Keypad Status (Active Low Bitmask)",
        valueHex = state.inputBufferHex,
        valueInt = state.inputBufferHex.removePrefix("0x").toIntOrNull(16) ?: 0x03FF,
        bitfieldDetails = listOf("Held Keys: ${state.lastInputKey}", "Active Low Bitmask: ${state.inputBufferHex}")
      ),
      IoRegisterEntry(
        addressHex = "0x04000102",
        name = "TM0CNT_H",
        description = "Timer 0 Control",
        valueHex = "0x0080",
        valueInt = 0x0080,
        bitfieldDetails = listOf("Timer Enabled: Yes", "IRQ on Overflow: No", "Prescaler: 1 cycle (16.78 MHz)")
      ),
      IoRegisterEntry(
        addressHex = "0x04000200",
        name = "IE (Interrupt Enable)",
        description = "Hardware Interrupt Enable Register",
        valueHex = "0x0001",
        valueInt = 0x0001,
        bitfieldDetails = listOf("V-Blank IRQ: Enabled", "H-Blank IRQ: Disabled", "Timer IRQ: Enabled", "Keypad IRQ: Disabled")
      )
    )
  }

  // --------------------------------------------------------------------------
  // VRAM PALETTES & OAM SPRITES
  // --------------------------------------------------------------------------

  fun getPaletteColors(): List<PaletteColorEntry> {
    val list = mutableListOf<PaletteColorEntry>()
    // 16 Background palettes x 16 colors = 256 colors
    val sampleBaseColors = listOf(
      0xFF000000, 0xFFFFFFFF, 0xFF244CA9, 0xFF3362C9,
      0xFF10B981, 0xFF059669, 0xFFEF4444, 0xFFF59E0B,
      0xFF8B5CF6, 0xFF06B6D4, 0xFFEAB308, 0xFFD97706,
      0xFF64748B, 0xFF334155, 0xFF1E293B, 0xFFF1F5F9
    )
    for (bank in 0 until 16) {
      for (i in 0 until 16) {
        val base = sampleBaseColors[(bank + i) % sampleBaseColors.size]
        val idx = bank * 16 + i
        list.add(
          PaletteColorEntry(
            index = idx,
            bank = bank,
            colorHex = base,
            rgbHexStr = String.format("#%06X", base and 0xFFFFFF),
            rawBgr15 = (idx * 128) and 0x7FFF
          )
        )
      }
    }
    return list
  }

  fun getOamSprites(): List<OamSpriteEntry> {
    val state = consoleEngine.gameState.value
    return listOf(
      OamSpriteEntry(
        id = 0,
        x = (state.playerX * 8).coerceIn(16, 220),
        y = (state.playerY * 8).coerceIn(16, 140),
        tileIndex = 12,
        sizeStr = "16x16 px",
        priority = 0,
        isHorizontalFlip = false,
        isVerticalFlip = false,
        paletteBank = 1
      ),
      OamSpriteEntry(
        id = 1,
        x = 180,
        y = 64,
        tileIndex = 24,
        sizeStr = if (state.isInBattle) "32x32 px" else "16x16 px",
        priority = 1,
        isHorizontalFlip = false,
        isVerticalFlip = false,
        paletteBank = if (state.isEnemyShiny) 4 else 2
      ),
      OamSpriteEntry(
        id = 2,
        x = 24,
        y = 120,
        tileIndex = 48,
        sizeStr = "8x8 px",
        priority = 2,
        isHorizontalFlip = false,
        isVerticalFlip = false,
        paletteBank = 0
      )
    )
  }
}
