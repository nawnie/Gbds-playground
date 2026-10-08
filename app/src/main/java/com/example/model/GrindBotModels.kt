package com.example.model

/**
 * High-level objective for the deterministic AI Grind Bot
 */
enum class GrindBotGoal(val displayName: String, val description: String) {
  SHINY_HUNT(
    displayName = "Shiny Hunt (Area Patrol)",
    description = "Patrols grass continuously, detects shiny encounters immediately, and pauses/locks for capture."
  ),
  LEVEL_UP(
    displayName = "Level Up Grinder",
    description = "Fights wild Pokémon until player reaches target level, managing HP and recovery."
  ),
  MONEY_FARM(
    displayName = "Money & Coin Farm",
    description = "Automates battles to farm maximum prize Pokédollars."
  ),
  CATCH_ALL(
    displayName = "Auto-Catcher Routine",
    description = "Detects wild species, throws Poké Balls automatically, and catalogs new Dex entries."
  )
}

/**
 * State machine stages for deterministic game automation
 */
enum class GrindBotState(val label: String, val badgeColorHex: Long) {
  STOPPED("IDLE / OFF", 0xFF64748B),
  PATROLLING_GRASS("PATROLLING GRASS", 0xFF10B981),
  BATTLE_ENGAGED("BATTLE ENGAGED", 0xFFF59E0B),
  BATTLE_FIGHTING("EXECUTING MOVE", 0xFF3B82F6),
  BATTLE_CATCHING("THROWING BALL", 0xFF8B5CF6),
  SHINY_LOCKED("✨ SHINY DETECTED!", 0xFFEAB308),
  HEALING_TRIP("HEALING RETREAT", 0xFF06B6D4),
  VICTORY_CLEAR("CLEARING DIALOG", 0xFF22C55E)
}

/**
 * Encounter log entry recorded by the grind bot
 */
data class EncounterRecord(
  val encounterNumber: Int,
  val pokemonName: String,
  val level: Int,
  val isShiny: Boolean,
  val actionTaken: String,
  val timestamp: Long = System.currentTimeMillis()
)

/**
 * Live telemetry and configuration for the Grind Bot
 */
data class GrindBotTelemetry(
  val isEnabled: Boolean = false,
  val goal: GrindBotGoal = GrindBotGoal.SHINY_HUNT,
  val currentState: GrindBotState = GrindBotState.STOPPED,
  val targetLevel: Int = 15,
  val targetMoney: Int = 50000,
  val totalEncounters: Int = 0,
  val totalBattlesWon: Int = 0,
  val totalShiniesFound: Int = 0,
  val totalBallsThrown: Int = 0,
  val shinyEncounterNumber: Int? = null,
  val lastEncounterName: String = "None",
  val lastEncounterShiny: Boolean = false,
  val statusMessage: String = "AI Grind Bot Standby. Choose goal and start.",
  val pauseOnShiny: Boolean = true,
  val autoHealThresholdHp: Int = 6,
  val patrolStepsCount: Int = 0,
  val recentEncounters: List<EncounterRecord> = emptyList()
)
