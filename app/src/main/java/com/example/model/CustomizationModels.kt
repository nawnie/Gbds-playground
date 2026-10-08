package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Handheld Shell Color Presets based on authentic Nintendo special editions
 */
enum class ShellColorPreset(
  val displayName: String,
  val shellBaseHex: Long,
  val shellHighlightHex: Long,
  val shellDeepHex: Long,
  val description: String
) {
  COBALT_BLUE(
    displayName = "Cobalt Blue",
    shellBaseHex = 0xFF244CA9,
    shellHighlightHex = 0xFF3362C9,
    shellDeepHex = 0xFF0C1B3F,
    description = "Iconic metallic blue launch edition for Game Boy Advance SP"
  ),
  ONYX_BLACK(
    displayName = "Onyx Black",
    shellBaseHex = 0xFF18181B,
    shellHighlightHex = 0xFF27272A,
    shellDeepHex = 0xFF09090B,
    description = "Sleek matte midnight black finish"
  ),
  FLAME_RED(
    displayName = "Flame Red",
    shellBaseHex = 0xFFDC2626,
    shellHighlightHex = 0xFFEF4444,
    shellDeepHex = 0xFF7F1D1D,
    description = "Vibrant metallic crimson red edition"
  ),
  PEARL_PINK(
    displayName = "Pearl Pink",
    shellBaseHex = 0xFFEC4899,
    shellHighlightHex = 0xFFF472B6,
    shellDeepHex = 0xFF831843,
    description = "Pearlescent rose metallic finish"
  ),
  ARCTIC_WHITE(
    displayName = "Arctic White",
    shellBaseHex = 0xFFE2E8F0,
    shellHighlightHex = 0xFFF8FAFC,
    shellDeepHex = 0xFF94A3B8,
    description = "Crisp glacier white with silver accents"
  ),
  EMERALD_RAYQUAZA(
    displayName = "Emerald Green",
    shellBaseHex = 0xFF059669,
    shellHighlightHex = 0xFF10B981,
    shellDeepHex = 0xFF064E3B,
    description = "Pokémon Emerald Rayquaza limited edition green"
  ),
  ATOMIC_PURPLE(
    displayName = "Atomic Purple",
    shellBaseHex = 0xFF7C3AED,
    shellHighlightHex = 0xFF8B5CF6,
    shellDeepHex = 0xFF4C1D95,
    description = "Semi-translucent atomic violet retro style"
  ),
  PIKACHU_CANARY(
    displayName = "Pikachu Yellow",
    shellBaseHex = 0xFFEAB308,
    shellHighlightHex = 0xFFFACC15,
    shellDeepHex = 0xFF713F12,
    description = "Pokémon Center Pikachu Canary Gold special edition"
  ),
  PLATINUM_SILVER(
    displayName = "Platinum Silver",
    shellBaseHex = 0xFF64748B,
    shellHighlightHex = 0xFF94A3B8,
    shellDeepHex = 0xFF334155,
    description = "Brushed metallic platinum SP AGS-101"
  ),
  CYBERPUNK_NEON(
    displayName = "Cyberpunk Cyan",
    shellBaseHex = 0xFF0891B2,
    shellHighlightHex = 0xFF06B6D4,
    shellDeepHex = 0xFF164E63,
    description = "High-tech synthwave luminescent cyan"
  )
}

/**
 * Controller Button Color Presets
 */
enum class ButtonColorPreset(
  val displayName: String,
  val buttonFaceHex: Long,
  val buttonTextHex: Long,
  val dpadHex: Long,
  val description: String
) {
  CLASSIC_CHARCOAL(
    displayName = "Classic Charcoal",
    buttonFaceHex = 0xFF232936,
    buttonTextHex = 0xFFE2E8F0,
    dpadHex = 0xFF29303D,
    description = "Original matte charcoal ABS plastic keys"
  ),
  SUPER_FAMICOM(
    displayName = "Super Famicom 4-Color",
    buttonFaceHex = 0xFFDC2626, // A=Red, B=Yellow handled dynamically
    buttonTextHex = 0xFFFFFFFF,
    dpadHex = 0xFF334155,
    description = "Multicolor SNES buttons (Red, Yellow, Blue, Green)"
  ),
  GAMECUBE_INDIGO(
    displayName = "GameCube Special",
    buttonFaceHex = 0xFF10B981, // Green A, Red B
    buttonTextHex = 0xFFFFFFFF,
    dpadHex = 0xFF6366F1,
    description = "Big green A and kidney bean red B buttons"
  ),
  PEARL_WHITE(
    displayName = "Pure White",
    buttonFaceHex = 0xFFF1F5F9,
    buttonTextHex = 0xFF0F172A,
    dpadHex = 0xFFE2E8F0,
    description = "Clean ceramic white with dark engraved text"
  ),
  CRIMSON_GLOW(
    displayName = "Crimson Ruby",
    buttonFaceHex = 0xFFE11D48,
    buttonTextHex = 0xFFFFFFFF,
    dpadHex = 0xFF18181B,
    description = "High-contrast ruby red action keys"
  ),
  NEON_CYAN(
    displayName = "Neon Cyan",
    buttonFaceHex = 0xFF06B6D4,
    buttonTextHex = 0xFF09090B,
    dpadHex = 0xFF1E293B,
    description = "Glowing cyberpunk electric cyan"
  ),
  GOLD_EDITION(
    displayName = "Imperial Gold",
    buttonFaceHex = 0xFFD97706,
    buttonTextHex = 0xFFFFFFFF,
    dpadHex = 0xFF78350F,
    description = "Luxurious anodized metallic gold buttons"
  )
}

/**
 * Haptic intensity levels
 */
enum class HapticProfile(
  val displayName: String,
  val durationMs: Long,
  val amplitude: Int,
  val description: String
) {
  OFF("Off (Silent)", 0, 0, "No vibration feedback on button presses"),
  LIGHT("Light Tick", 6, 60, "Subtle micro-tick for quiet tactile confirmation"),
  MEDIUM("Standard Click", 14, 150, "Balanced tactile response imitating tactile microswitch"),
  CRISP_STRONG("Crisp Heavy", 26, 255, "Deep punchy rumble with maximum tactile feedback"),
  RETRO_CLICK("Retro Mechanical", 18, 200, "Double micro-pulse mimicking spring-loaded membrane switches")
}

/**
 * Complete handheld appearance and haptic configuration
 */
data class ConsoleCustomizationConfig(
  val shellPreset: ShellColorPreset = ShellColorPreset.COBALT_BLUE,
  val customShellHex: Long = ShellColorPreset.COBALT_BLUE.shellBaseHex,
  val customShellHighlightHex: Long = ShellColorPreset.COBALT_BLUE.shellHighlightHex,
  val customShellDeepHex: Long = ShellColorPreset.COBALT_BLUE.shellDeepHex,
  val buttonPreset: ButtonColorPreset = ButtonColorPreset.CLASSIC_CHARCOAL,
  val customButtonFaceHex: Long = ButtonColorPreset.CLASSIC_CHARCOAL.buttonFaceHex,
  val customButtonTextHex: Long = ButtonColorPreset.CLASSIC_CHARCOAL.buttonTextHex,
  val customDpadHex: Long = ButtonColorPreset.CLASSIC_CHARCOAL.dpadHex,
  val customAccentHex: Long = 0xFF22C55E, // Power LED
  val isCustomColorActive: Boolean = false,
  val hapticProfile: HapticProfile = HapticProfile.MEDIUM,
  val hapticOnPress: Boolean = true,
  val hapticOnRelease: Boolean = true,
  val hapticOnTouchScreen: Boolean = true
) {
  val activeShellColor: Color
    get() = if (isCustomColorActive) Color(customShellHex) else Color(shellPreset.shellBaseHex)

  val activeShellHighlightColor: Color
    get() = if (isCustomColorActive) Color(customShellHighlightHex) else Color(shellPreset.shellHighlightHex)

  val activeShellDeepColor: Color
    get() = if (isCustomColorActive) Color(customShellDeepHex) else Color(shellPreset.shellDeepHex)

  val activeButtonColor: Color
    get() = if (isCustomColorActive) Color(customButtonFaceHex) else Color(buttonPreset.buttonFaceHex)

  val activeButtonTextColor: Color
    get() = if (isCustomColorActive) Color(customButtonTextHex) else Color(buttonPreset.buttonTextHex)

  val activeDpadColor: Color
    get() = if (isCustomColorActive) Color(customDpadHex) else Color(buttonPreset.dpadHex)

  val isFamicomButtons: Boolean
    get() = buttonPreset == ButtonColorPreset.SUPER_FAMICOM
}
