package com.example.model

/**
 * Supported handheld virtual console systems
 */
enum class GameConsoleMode(val displayName: String, val screenAspect: String, val buttonSet: String) {
  GBA("Game Boy Advance SP", "3:2 (240x160)", "D-Pad, A, B, L, R, Start, Select"),
  NDS_3DS("Nintendo 3DS", "Dual 5:3 / 4:3", "D-Pad, A, B, X, Y, L, R, Touch, Start, Select")
}

/**
 * Built-in game cartridge simulations
 */
enum class GameScenario(
  val id: String,
  val title: String,
  val platform: GameConsoleMode,
  val defaultZone: String,
  val initialHp: Int,
  val maxHp: Int,
  val initialLevel: Int,
  val initialCoins: Int
) {
  POKEMON_RED(
    id = "poke_red",
    title = "Pokémon Red Version (GB/GBA)",
    platform = GameConsoleMode.GBA,
    defaultZone = "Pallet Town - Route 1",
    initialHp = 20,
    maxHp = 20,
    initialLevel = 5,
    initialCoins = 1000
  ),
  POKEMON_EMERALD(
    id = "poke_emerald",
    title = "Pokémon Emerald Version",
    platform = GameConsoleMode.GBA,
    defaultZone = "Littleroot Town - Route 101",
    initialHp = 22,
    maxHp = 22,
    initialLevel = 5,
    initialCoins = 3000
  ),
  ZELDA_MINISH(
    id = "zelda_minish",
    title = "The Legend of Zelda: The Minish Cap",
    platform = GameConsoleMode.GBA,
    defaultZone = "Hyrule Town - Castle Courtyard",
    initialHp = 12,
    maxHp = 12,
    initialLevel = 1,
    initialCoins = 150
  ),
  MARIO_ADVANCE(
    id = "mario_adv",
    title = "Super Mario Advance 4: Super Mario Bros 3",
    platform = GameConsoleMode.GBA,
    defaultZone = "World 1-1 Grass Land",
    initialHp = 3,
    maxHp = 3,
    initialLevel = 1,
    initialCoins = 42
  ),
  POKEMON_SUN_3DS(
    id = "poke_sun",
    title = "Pokémon Sun & Moon (3DS)",
    platform = GameConsoleMode.NDS_3DS,
    defaultZone = "Melemele Island - Route 1",
    initialHp = 24,
    maxHp = 24,
    initialLevel = 7,
    initialCoins = 5200
  ),
  ZELDA_OOT_3DS(
    id = "zelda_oot",
    title = "The Legend of Zelda: Ocarina of Time 3D",
    platform = GameConsoleMode.NDS_3DS,
    defaultZone = "Kokiri Forest - Great Deku Tree",
    initialHp = 16,
    maxHp = 16,
    initialLevel = 1,
    initialCoins = 99
  )
}

/**
 * Categories for computer vision object detection
 */
enum class VisionCategory {
  PLAYER,
  ENEMY,
  NPC,
  DIALOG,
  HP_BAR,
  ITEM,
  WAYPOINT,
  OBSTACLE
}

/**
 * Real-time vision detection box overlayed on screen
 */
data class VisionDetection(
  val id: String,
  val label: String,
  val category: VisionCategory,
  val xNorm: Float, // Normalized 0.0 - 1.0 on virtual screen
  val yNorm: Float,
  val widthNorm: Float,
  val heightNorm: Float,
  val confidence: Float,
  val extraInfo: String = ""
)

/**
 * Real-time memory watch entry
 */
data class MemoryWatchEntry(
  val addressHex: String,
  val label: String,
  val valueHex: String,
  val decimalValue: Int,
  val isFrozen: Boolean = false,
  val description: String = ""
)

/**
 * Memory cheat switch
 */
data class GameCheat(
  val id: String,
  val title: String,
  val description: String,
  val addressHex: String,
  val activeValueHex: String,
  val isEnabled: Boolean,
  val category: String // "STATS", "BATTLE", "MOVEMENT", "RESOURCES"
)

/**
 * Snapshot of emulated virtual console state
 */
data class GameStateSnapshot(
  val scenario: GameScenario,
  val consoleMode: GameConsoleMode,
  val frameNumber: Long = 0L,
  val playerX: Int = 12,
  val playerY: Int = 18,
  val playerHp: Int = 22,
  val playerMaxHp: Int = 22,
  val playerLevel: Int = 5,
  val coins: Int = 3000,
  val zoneName: String = "Littleroot Town",
  val dialogText: String = "Now tell me. Are you a boy or a girl?",
  val isInBattle: Boolean = false,
  val enemyName: String? = null,
  val enemyHp: Int = 0,
  val enemyMaxHp: Int = 0,
  val activeCheatsCount: Int = 0,
  val lastInputKey: String = "IDLE",
  val inputBufferHex: String = "0x0000",
  val detections: List<VisionDetection> = emptyList(),
  val memoryWatch: List<MemoryWatchEntry> = emptyList()
)

/**
 * Virtual controller button signals
 */
enum class GamepadKey {
  UP, DOWN, LEFT, RIGHT,
  A, B, X, Y,
  L, R,
  START, SELECT, MENU
}
