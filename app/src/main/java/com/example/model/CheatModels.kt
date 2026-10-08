package com.example.model

/**
 * Supported handheld cheat device engines
 */
enum class CheatEngineType(val displayName: String, val syntaxHint: String, val prefix: String) {
  GAMESHARK("GameShark (GBA/GBC)", "8 or 16-hex code: XXXXXXXX YYYYYYYY or 01XXYYZZ", "GS"),
  CODEBREAKER("Codebreaker (GBA)", "12-hex code: XXXXXXXX YYYY (8XXXXXXX / 3XXXXXXX)", "CB"),
  ACTION_REPLAY("Action Replay (DS/3DS)", "16-hex code: 0XXXXXXX YYYYYYYY (32-bit RAM poke)", "AR")
}

/**
 * Cheat code entry with decoded RAM address and payload
 */
data class CheatCodeEntry(
  val id: String,
  val title: String,
  val description: String,
  val engineType: CheatEngineType,
  val rawCode: String,
  val targetAddressHex: String,
  val targetValueHex: String,
  val targetScenario: GameScenario?,
  val isEnabled: Boolean = false,
  val isMasterCode: Boolean = false,
  val category: String = "STATS", // STATS, ITEMS, MOVEMENT, BATTLE, UNLOCKS
  val verificationStatus: String = "VALIDATED" // VALIDATED, ACTIVE, HOOKED, ERROR
)

/**
 * Pre-configured authentic cheats across Game Boy Advance and Nintendo 3DS games
 */
object AuthenticCheatDatabase {
  val defaultCheats: List<CheatCodeEntry> = listOf(
    // --- POKEMON RED (GB/GBA) ---
    CheatCodeEntry(
      id = "gs_red_master",
      title = "Pokémon Red Master Code (Must Be On)",
      description = "Hooks into interrupt vector to allow memory interception",
      engineType = CheatEngineType.GAMESHARK,
      rawCode = "010000D0",
      targetAddressHex = "0x02000000",
      targetValueHex = "0x0001",
      targetScenario = GameScenario.POKEMON_RED,
      isEnabled = true,
      isMasterCode = true,
      category = "UNLOCKS"
    ),
    CheatCodeEntry(
      id = "gs_red_money",
      title = "Max Money $999,999",
      description = "Pokes wallet memory bank with maximum coin registers",
      engineType = CheatEngineType.GAMESHARK,
      rawCode = "019946D3\n019947D3\n019948D3",
      targetAddressHex = "0x02024090",
      targetValueHex = "0x000F423F",
      targetScenario = GameScenario.POKEMON_RED,
      isEnabled = false,
      category = "ITEMS"
    ),
    CheatCodeEntry(
      id = "gs_red_masterball",
      title = "Infinite Master Balls (Slot 1)",
      description = "Injects 99 Master Balls into Item Bag first pocket",
      engineType = CheatEngineType.GAMESHARK,
      rawCode = "01017CCF\n01637DCF",
      targetAddressHex = "0x02025800",
      targetValueHex = "0x0063",
      targetScenario = GameScenario.POKEMON_RED,
      isEnabled = false,
      category = "ITEMS"
    ),
    CheatCodeEntry(
      id = "gs_red_walk_walls",
      title = "Walk Through Walls (Ghost Clip)",
      description = "Overwrites boundary collision check routine in WRAM",
      engineType = CheatEngineType.GAMESHARK,
      rawCode = "010156D0",
      targetAddressHex = "0x020370E4",
      targetValueHex = "0x0001",
      targetScenario = GameScenario.POKEMON_RED,
      isEnabled = false,
      category = "MOVEMENT"
    ),
    CheatCodeEntry(
      id = "gs_red_rarecandy",
      title = "Unlimited Rare Candies (Free Lv 100)",
      description = "Freezes Item Slot 2 with 99x Rare Candies",
      engineType = CheatEngineType.GAMESHARK,
      rawCode = "01287ECF\n01637FCF",
      targetAddressHex = "0x02025804",
      targetValueHex = "0x0063",
      targetScenario = GameScenario.POKEMON_RED,
      isEnabled = false,
      category = "ITEMS"
    ),
    CheatCodeEntry(
      id = "gs_red_catch_rate",
      title = "100% Guaranteed Catch Rate",
      description = "Forces catch formula calculation check to always succeed",
      engineType = CheatEngineType.GAMESHARK,
      rawCode = "01FFD7CF",
      targetAddressHex = "0x02024042",
      targetValueHex = "0x00FF",
      targetScenario = GameScenario.POKEMON_RED,
      isEnabled = false,
      category = "BATTLE"
    ),

    // --- POKEMON EMERALD (GBA) ---
    CheatCodeEntry(
      id = "cb_eme_master",
      title = "Emerald Master Hook (v1.0)",
      description = "Codebreaker Master Code enabling RAM writes",
      engineType = CheatEngineType.CODEBREAKER,
      rawCode = "00006FA7 000A\n1006AF8C 0007",
      targetAddressHex = "0x02000000",
      targetValueHex = "0x000A",
      targetScenario = GameScenario.POKEMON_EMERALD,
      isEnabled = true,
      isMasterCode = true,
      category = "UNLOCKS"
    ),
    CheatCodeEntry(
      id = "cb_eme_hp",
      title = "Infinite Battle HP (God Mode)",
      description = "Pokes leader battle HP to max immediately on damage",
      engineType = CheatEngineType.CODEBREAKER,
      rawCode = "8202402C 0014",
      targetAddressHex = "0x0202402C",
      targetValueHex = "0x0014",
      targetScenario = GameScenario.POKEMON_EMERALD,
      isEnabled = false,
      category = "BATTLE"
    ),
    CheatCodeEntry(
      id = "cb_eme_money",
      title = "Max Cash 999,999 Pokédollars",
      description = "16-bit split poke to Money registers",
      engineType = CheatEngineType.CODEBREAKER,
      rawCode = "82024090 423F\n82024092 000F",
      targetAddressHex = "0x02024090",
      targetValueHex = "0x000F423F",
      targetScenario = GameScenario.POKEMON_EMERALD,
      isEnabled = false,
      category = "ITEMS"
    ),
    CheatCodeEntry(
      id = "cb_eme_speed",
      title = "4x Hyper Movement Speed",
      description = "Modifies player step cadence clock",
      engineType = CheatEngineType.CODEBREAKER,
      rawCode = "82038010 0004",
      targetAddressHex = "0x02038010",
      targetValueHex = "0x0004",
      targetScenario = GameScenario.POKEMON_EMERALD,
      isEnabled = false,
      category = "MOVEMENT"
    ),
    CheatCodeEntry(
      id = "cb_eme_ko",
      title = "One-Hit KO Opponent",
      description = "Forces opponent HP register to zero when attack lands",
      engineType = CheatEngineType.CODEBREAKER,
      rawCode = "82024040 0000",
      targetAddressHex = "0x02024040",
      targetValueHex = "0x0000",
      targetScenario = GameScenario.POKEMON_EMERALD,
      isEnabled = false,
      category = "BATTLE"
    ),

    // --- ZELDA MINISH CAP (GBA) ---
    CheatCodeEntry(
      id = "gs_zelda_hp",
      title = "Infinite Hearts (Link Invincible)",
      description = "GameShark code freezing Link's health at 20 hearts",
      engineType = CheatEngineType.GAMESHARK,
      rawCode = "02002AEA 14",
      targetAddressHex = "0x0202402C",
      targetValueHex = "0x0014",
      targetScenario = GameScenario.ZELDA_MINISH,
      isEnabled = false,
      category = "STATS"
    ),
    CheatCodeEntry(
      id = "cb_zelda_rupees",
      title = "Max 999 Rupees",
      description = "Pokes rupee counter to 999 max wallet capacity",
      engineType = CheatEngineType.CODEBREAKER,
      rawCode = "82002B00 03E7",
      targetAddressHex = "0x02024090",
      targetValueHex = "0x03E7",
      targetScenario = GameScenario.ZELDA_MINISH,
      isEnabled = false,
      category = "ITEMS"
    ),

    // --- NINTENDO 3DS GAMES (ACTION REPLAY DS / 3DS) ---
    CheatCodeEntry(
      id = "ar_3ds_sun_money",
      title = "Action Replay: Max 9,999,999 Pokédollars (3DS)",
      description = "32-bit direct RAM poke for 3DS save registers",
      engineType = CheatEngineType.ACTION_REPLAY,
      rawCode = "02024090 000F423F",
      targetAddressHex = "0x02024090",
      targetValueHex = "0x000F423F",
      targetScenario = GameScenario.POKEMON_SUN_3DS,
      isEnabled = false,
      category = "ITEMS"
    ),
    CheatCodeEntry(
      id = "ar_3ds_sun_hp",
      title = "Action Replay: Full Team Infinite HP",
      description = "Freezes active partner slot HP values in 3DS memory",
      engineType = CheatEngineType.ACTION_REPLAY,
      rawCode = "0202402C 00000018",
      targetAddressHex = "0x0202402C",
      targetValueHex = "0x0018",
      targetScenario = GameScenario.POKEMON_SUN_3DS,
      isEnabled = false,
      category = "BATTLE"
    ),
    CheatCodeEntry(
      id = "ar_3ds_oot_rupees",
      title = "Action Replay: 500 Giant's Wallet Rupees",
      description = "Pokes Zelda OoT 3D rupee slot",
      engineType = CheatEngineType.ACTION_REPLAY,
      rawCode = "02024090 000001F4",
      targetAddressHex = "0x02024090",
      targetValueHex = "0x01F4",
      targetScenario = GameScenario.ZELDA_OOT_3DS,
      isEnabled = false,
      category = "ITEMS"
    )
  )
}
