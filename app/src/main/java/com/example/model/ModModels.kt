package com.example.model

/**
 * Types of game modifications
 */
enum class ModType(val displayName: String, val badgeColorHex: Long) {
  ROM_HACK("ROM Overhaul", 0xFF8B5CF6),
  PATCH_IPS("IPS/BPS Patch", 0xFF06B6D4),
  GAMEPLAY_RULE("Gameplay Hook", 0xFF10B981),
  GRAPHICS_FILTER("Graphics & Shader", 0xFFF59E0B)
}

/**
 * ROM Hack / Mod descriptor
 */
data class GameModEntry(
  val id: String,
  val title: String,
  val version: String,
  val author: String,
  val description: String,
  val targetScenario: GameScenario,
  val modType: ModType,
  val patchChecksum: String,
  val patchSizeKb: Int,
  val features: List<String>,
  val isApplied: Boolean = false,
  val requiresRestart: Boolean = false
)

/**
 * Dynamic in-engine gameplay rule modifiers (can be toggled on the fly)
 */
data class GameplayRuleModifier(
  val id: String,
  val name: String,
  val description: String,
  val category: String,
  val isEnabled: Boolean = false,
  val memoryPatchHook: String = ""
)

/**
 * Catalog of community mods and gameplay modifiers
 */
object ModCatalog {
  val defaultMods: List<GameModEntry> = listOf(
    GameModEntry(
      id = "mod_rad_red",
      title = "Pokémon Radical Red",
      version = "v4.1 Complete",
      author = "soupercell & team",
      description = "Comprehensive difficulty & feature overhaul bringing Gen 1-9 Pokémon, Mega Evolution, and competitive AI to FireRed/Red engine.",
      targetScenario = GameScenario.POKEMON_RED,
      modType = ModType.ROM_HACK,
      patchChecksum = "CRC32: 9C8A41E2",
      patchSizeKb = 32768,
      features = listOf(
        "All Pokémon from Generations 1 through 9",
        "Modern battle engine (Physical/Special split, Fairy type)",
        "Hardcore boss AI with competitive EV/IV spreads",
        "DexNav tool for hunting hidden abilities and egg moves",
        "Built-in Level Caps preventing over-leveling"
      ),
      isApplied = false
    ),
    GameModEntry(
      id = "mod_eme_rogue",
      title = "Pokémon Emerald Rogue",
      version = "v2.0 EX",
      author = "Pokabbie",
      description = "Transforms Emerald into a full roguelike adventure with procedural routes, permadeath challenges, and hub upgrades.",
      targetScenario = GameScenario.POKEMON_EMERALD,
      modType = ModType.ROM_HACK,
      patchChecksum = "CRC32: 3D14F98B",
      patchSizeKb = 16384,
      features = listOf(
        "Procedurally generated route progression",
        "Adventure hub with unlockable buildings & perks",
        "Permadeath runs with reward points",
        "Custom quest board & legendary raid encounters"
      ),
      isApplied = false
    ),
    GameModEntry(
      id = "mod_firered_randomizer",
      title = "Pokémon 898 Universal Randomizer",
      version = "v1.8",
      author = "Dabomstew / Community",
      description = "Randomizes wild encounters, trainer rosters, TM compatibility, and starter Pokémon across all regions.",
      targetScenario = GameScenario.POKEMON_RED,
      modType = ModType.PATCH_IPS,
      patchChecksum = "SHA1: 4F2A11E0",
      patchSizeKb = 8192,
      features = listOf(
        "Completely randomized wild Pokémon encounters",
        "Randomized gym leader signature creatures",
        "Uncapped evolution levels (Trade evos evolve via Level 37)"
      ),
      isApplied = false
    ),
    GameModEntry(
      id = "mod_60fps_fluid",
      title = "60 FPS Fluid Animation & Turbo Engine",
      version = "v3.2",
      author = "Krikzz & VC Team",
      description = "Uncaps internal rendering clock from 30 FPS to silky smooth 60 FPS interpolated overworld movement.",
      targetScenario = GameScenario.POKEMON_RED,
      modType = ModType.PATCH_IPS,
      patchChecksum = "CRC32: 12B88FF0",
      patchSizeKb = 512,
      features = listOf(
        "60 FPS smooth camera panning in all routes",
        "Zero input latency buffer bypass",
        "Instant text rendering without slow typewriter pause"
      ),
      isApplied = true
    ),
    GameModEntry(
      id = "mod_hd_palettes",
      title = "Vibrant HD Color Palettes & Sprites",
      version = "v2.5",
      author = "SprintersHQ",
      description = "Replaces classic muted GBA color matrix with vibrant high-contrast dynamic gamma palettes and remastered sprites.",
      targetScenario = GameScenario.POKEMON_EMERALD,
      modType = ModType.GRAPHICS_FILTER,
      patchChecksum = "CRC32: 78AA4002",
      patchSizeKb = 2048,
      features = listOf(
        "High-gamut RGB palette remaster",
        "Clean pixel-art dithering reduction",
        "Custom shiny star particle burst animations"
      ),
      isApplied = false
    ),
    GameModEntry(
      id = "mod_ai_copilot_patch",
      title = "EdgePilot Battle AI Assistant Patch",
      version = "v1.0",
      author = "EdgePilot Core",
      description = "Injects real-time DMA memory hooks allowing EdgePilot AI to read wild encounter IVs, EVs, and optimal movesets.",
      targetScenario = GameScenario.POKEMON_RED,
      modType = ModType.GAMEPLAY_RULE,
      patchChecksum = "CRC32: A0B1C2D3",
      patchSizeKb = 256,
      features = listOf(
        "Live Battle Type Weakness HUD overlay",
        "Instant shiny detection trigger in memory",
        "Automated damage calculation matrix"
      ),
      isApplied = true
    )
  )

  val defaultGameplayRules: List<GameplayRuleModifier> = listOf(
    GameplayRuleModifier(
      id = "rule_exp_share",
      name = "Modern Exp. Share (Full Party)",
      description = "All party members receive 50% EXP from battles even without entering the field.",
      category = "PROGRESSION",
      isEnabled = true,
      memoryPatchHook = "0x02038700"
    ),
    GameplayRuleModifier(
      id = "rule_nuzlocke",
      name = "Nuzlocke Hardcore Permadeath",
      description = "Fainted Pokémon cannot be revived or used in future battles; must be released or boxed.",
      category = "CHALLENGE",
      isEnabled = false,
      memoryPatchHook = "0x02038704"
    ),
    GameplayRuleModifier(
      id = "rule_fast_text",
      name = "Instant 10x Text Speed",
      description = "Eliminates dialogue typing animation delays across all dialog boxes.",
      category = "QUALITY_OF_LIFE",
      isEnabled = true,
      memoryPatchHook = "0x02038708"
    ),
    GameplayRuleModifier(
      id = "rule_level_cap",
      name = "Gym Leader Level Cap Enforcer",
      description = "Disables EXP gain once reaching the current gym leader's highest Pokémon level.",
      category = "CHALLENGE",
      isEnabled = false,
      memoryPatchHook = "0x0203870C"
    ),
    GameplayRuleModifier(
      id = "rule_infinite_tms",
      name = "Reusable TMs (Gen 5+ Rule)",
      description = "Technical Machines do not break after being taught to a creature.",
      category = "QUALITY_OF_LIFE",
      isEnabled = true,
      memoryPatchHook = "0x02038710"
    ),
    GameplayRuleModifier(
      id = "rule_auto_run",
      name = "Permanent Auto-Run / Turbo Shoes",
      description = "Player character runs at B-dash speed automatically without holding the B button.",
      category = "MOVEMENT",
      isEnabled = false,
      memoryPatchHook = "0x02038714"
    )
  )
}
