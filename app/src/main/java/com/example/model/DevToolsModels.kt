package com.example.model

/**
 * GBA and 3DS Memory Map Regions
 */
enum class MemoryRegion(
  val displayName: String,
  val baseAddressHex: String,
  val sizeBytes: Int,
  val description: String
) {
  EWRAM("EWRAM (256 KB)", "0x02000000", 262144, "External Work RAM (Game state, Party, Variables)"),
  IWRAM("IWRAM (32 KB)", "0x03000000", 32768, "Fast Internal Work RAM (Stacks, DMA buffers, Time-critical)"),
  IO_REGS("I/O Registers", "0x04000000", 1024, "Hardware LCD, Timers, DMA, Keypad, Interrupt registers"),
  PAL_RAM("Palette RAM (1 KB)", "0x05000000", 1024, "Color palettes (16 BG palettes + 16 OBJ palettes)"),
  VRAM("VRAM (96 KB)", "0x06000000", 98304, "Video RAM (Tile data, Background maps)"),
  OAM("OAM RAM (1 KB)", "0x07000000", 1024, "Object Attribute Memory (128 hardware sprites)")
}

/**
 * Single 16-byte row formatted for the Hex Viewer
 */
data class MemoryHexRow(
  val addressHex: String,
  val addressInt: Int,
  val bytes: List<Int>, // 16 bytes (0-255)
  val ascii: String
)

/**
 * Hardware OAM Sprite entry
 */
data class OamSpriteEntry(
  val id: Int,
  val x: Int,
  val y: Int,
  val tileIndex: Int,
  val sizeStr: String,
  val priority: Int,
  val isHorizontalFlip: Boolean,
  val isVerticalFlip: Boolean,
  val paletteBank: Int
)

/**
 * Hardware Palette Color entry (15-bit BGR converted to 32-bit RGB)
 */
data class PaletteColorEntry(
  val index: Int,
  val bank: Int,
  val colorHex: Long,
  val rgbHexStr: String,
  val rawBgr15: Int
)

/**
 * Hardware I/O Register definition
 */
data class IoRegisterEntry(
  val addressHex: String,
  val name: String,
  val description: String,
  val valueHex: String,
  val valueInt: Int,
  val bitfieldDetails: List<String>
)

/**
 * Developer log categories for filtering
 */
enum class DevLogCategory(val label: String, val badgeColorHex: Long) {
  ALL("ALL", 0xFF64748B),
  SYSTEM("SYSTEM", 0xFF38BDF8),
  MEMORY("MEMORY", 0xFF10B981),
  GRIND_BOT("GRIND BOT", 0xFFF59E0B),
  AI_CV("AI CV", 0xFF8B5CF6),
  CHEAT_HOOK("CHEATS", 0xFFEF4444),
  MOD_LOADER("MODS", 0xFF06B6D4),
  INPUT("INPUT", 0xFFEAB308)
}

/**
 * Real-time diagnostic event entry
 */
data class DevLogMessage(
  val id: Long = System.nanoTime(),
  val timestamp: Long = System.currentTimeMillis(),
  val category: DevLogCategory,
  val tag: String,
  val message: String,
  val level: String = "INFO" // INFO, WARN, ERROR, DEBUG
)
