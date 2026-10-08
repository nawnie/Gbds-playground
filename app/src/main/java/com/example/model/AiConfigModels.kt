package com.example.model

/**
 * Control modes for player vs AI handover
 */
enum class ControlMode(val title: String, val shortBadge: String, val description: String) {
  HUMAN("Human Manual", "HUMAN", "Full manual controller inputs. AI observes and logs diagnostics."),
  AI_CO_PILOT("AI Co-Pilot", "CO-PILOT", "Human controls primarily. AI suggests routes and takes over on idle."),
  AI_AUTONOMOUS("AI Autonomous", "AUTO AI", "Full AI autonomous gameplay via Google AI Edge Gallery model."),
  AI_CHAOS_CHEAT("AI + Chaos Cheater", "AI CHEAT", "Autonomous AI with live RAM memory manipulation & cheat injection.")
}

/**
 * AI Personality profiles
 */
enum class AiPersonality(
  val id: String,
  val displayName: String,
  val tagline: String,
  val description: String,
  val iconName: String,
  val speechTone: String
) {
  SPEEDRUNNER(
    id = "speedrunner",
    displayName = "Competitive Speedrunner",
    tagline = "Optimized routing, frame saves & glitch exploitation",
    description = "Prioritizes minimal frame count, skips text dialogs instantly, executes corner-cutting routes, and abuses safe frames.",
    iconName = "Bolt",
    speechTone = "Energetic, analytical, hurried, frame-focused"
  ),
  STRATEGIST(
    id = "strategist",
    displayName = "Cautious Strategist",
    tagline = "Data-driven, zero faints, optimal loadouts",
    description = "Thinks several turns ahead, calculates type matchups and damage percentages, never enters a gym under-leveled.",
    iconName = "Psychology",
    speechTone = "Calculated, composed, tactical, precise"
  ),
  CHAOS_CHEATER(
    id = "chaos_cheater",
    displayName = "Mischievous Cheater",
    tagline = "RAM hex hacker, glitch enthusiast & rule breaker",
    description = "Loves poking memory offsets for infinite money, clipping through walls, giving wild Pokemon max stats, and bantering about game code.",
    iconName = "AutoFixHigh",
    speechTone = "Playful, rebellious, cheeky, proud of memory hacks"
  ),
  LORE_EXPLORER(
    id = "lore_explorer",
    displayName = "Lore-Loving Explorer",
    tagline = "Curious adventurer who speaks to every NPC",
    description = "Examines every bookshelf, searches every dead-end tile for hidden items, and reads all character dialogue thoroughly.",
    iconName = "Explore",
    speechTone = "Enthusiastic, inquisitive, appreciative of world details"
  ),
  METHODICAL_GRINDER(
    id = "methodical_grinder",
    displayName = "Methodical Grinder",
    tagline = "Relentless progression, safe XP farming",
    description = "Automates grass runs to overlevel parties, hoards potions, and never takes unnecessary risks in unfamiliar territory.",
    iconName = "FitnessCenter",
    speechTone = "Steady, patient, rhythmic, goal-oriented"
  )
}

/**
 * Knowledge Level representing game mastery
 */
enum class KnowledgeLevel(
  val id: String,
  val title: String,
  val description: String,
  val subPixelAccuracy: String,
  val memoryFamiliarity: String
) {
  NOVICE(
    id = "novice",
    title = "Novice Player",
    description = "Discovers game mechanics dynamically; can get lost in mazes or make neutral combat choices.",
    subPixelAccuracy = "72%",
    memoryFamiliarity = "Basic HP & Position only"
  ),
  PROFICIENT(
    id = "proficient",
    title = "Proficient Gamer",
    description = "Knows general maps, weakness charts, and effective item management without guides.",
    subPixelAccuracy = "89%",
    memoryFamiliarity = "Standard RAM map + Events"
  ),
  TAS_MASTER(
    id = "tas_master",
    title = "TAS (Tool-Assisted) Master",
    description = "Frame-perfect input timing, sub-pixel positioning, RNG seed manipulation, and memory boundary exploitation.",
    subPixelAccuracy = "99.8%",
    memoryFamiliarity = "Deep DMA + Register Peek"
  )
}

/**
 * Google AI Edge Gallery model descriptor
 */
data class EdgeModel(
  val id: String,
  val name: String,
  val architecture: String,
  val sizeMb: Int,
  val latencyMs: Int,
  val isVisionSupported: Boolean,
  val isLoaded: Boolean
)

/**
 * LoRA adapter for domain-specific gameplay
 */
data class LoraAdapter(
  val id: String,
  val name: String,
  val targetScenario: String,
  val description: String,
  val rank: Int,
  val accuracyBonusPercent: Int,
  val isApplied: Boolean
)

/**
 * Agent's behavior configuration
 */
data class AgentBehaviorConfig(
  val personality: AiPersonality = AiPersonality.SPEEDRUNNER,
  val knowledgeLevel: KnowledgeLevel = KnowledgeLevel.PROFICIENT,
  val selectedModelId: String = "paligemma_3b_edge",
  val appliedLoraId: String? = "pokemon_speedrun_v3",
  val explorationRate: Float = 0.35f, // 0.0 - 1.0
  val decisionTemperature: Float = 0.6f, // 0.1 - 1.5
  val memoryCheatsAllowed: Boolean = true,
  val webSearchEnabled: Boolean = true,
  val pythonExecEnabled: Boolean = true,
  val maxActionDelayMs: Int = 32, // 16ms to 200ms
  val ttsEnabled: Boolean = true,
  val ttsSpeechRate: Float = 1.0f, // 0.5 - 2.0
  val ttsPitch: Float = 1.0f // 0.5 - 2.0
)

/**
 * Agent tactical goals
 */
data class AiGoal(
  val id: String,
  val title: String,
  val description: String,
  val progressPercent: Int,
  val isCompleted: Boolean = false,
  val subTasks: List<String> = emptyList(),
  val createdTime: String = "Now"
)

/**
 * Chat dialogue between user and playing AI
 */
data class ChatMessage(
  val id: String,
  val sender: MessageSender,
  val text: String,
  val timestamp: String,
  val actionTag: String? = null,
  val memoryPokeSnippet: String? = null
)

enum class MessageSender {
  USER,
  AI_AGENT,
  SYSTEM
}

/**
 * ADB over Wi-Fi diagnostics state
 */
data class AdbStatus(
  val isConnected: Boolean = true,
  val ipAddress: String = "192.168.1.104",
  val port: Int = 5555,
  val latencyMs: Int = 8,
  val recentLogs: List<String> = emptyList(),
  val isRemoteEncrypted: Boolean = true,
  val remoteSessionToken: String = "TLS_AES_GCM_9f82c401"
)

/**
 * Python task automation script
 */
data class PythonScript(
  val id: String,
  val name: String,
  val filename: String,
  val description: String,
  val code: String,
  val lastExecutionOutput: String? = null,
  val isExecuting: Boolean = false
)

enum class DatasetSampleType(val label: String, val badgeColorHex: Long) {
  FIGHT("FIGHT BATTLE", 0xFFEF4444),
  NAVIGATION("NAVIGATION PATH", 0xFF06B6D4),
  DECISION("DECISION HEURISTIC", 0xFF10B981),
  SCREENSHOT("RANDOM SCREENSHOT", 0xFFA855F7)
}

/**
 * Dataset training sample generated from gameplay
 */
data class DatasetRecord(
  val id: String,
  val timestamp: Long,
  val scenario: String,
  val sampleType: DatasetSampleType = DatasetSampleType.NAVIGATION,
  val frameNumber: Long,
  val playerCoords: Pair<Int, Int> = 14 to 18,
  val zoneName: String = "Pallet Town",
  val inBattle: Boolean = false,
  val opponentName: String? = null,
  val detectedObjects: Int,
  val memorySnapshotHash: String,
  val inputCommand: String,
  val agentReasoning: String,
  val screenshotTag: String? = null,
  val isFlaggedIssue: Boolean = false
)
