package com.example.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConsoleCustomizationConfig
import com.example.model.GamepadKey
import com.example.model.GameStateSnapshot
import com.example.model.GrindBotState
import com.example.model.GrindBotTelemetry

// Cobalt Metallic Blue Palette exactly matching the physical GBA SP photo
private val CobaltShellOuter = Color(0xFF142B66)
private val CobaltShellMain = Color(0xFF1A3B8B)
private val CobaltShellBright = Color(0xFF244CA9)
private val CobaltShellHighlight = Color(0xFF3362C9)
private val CobaltDeepWell = Color(0xFF0C1B3F)
private val DpadCharcoal = Color(0xFF29303D)
private val ButtonCharcoal = Color(0xFF232936)
private val SilkscreenSilver = Color(0xFFCBD5E1)
private val LedActiveGreen = Color(0xFF22C55E)

/**
 * Authentic Cobalt Blue Game Boy Advance SP Handheld Console.
 * Backed by a true input contract (KeyDown, KeyUp, simultaneous multi-touch, held states,
 * cancellation) and visible ownership tracking.
 */
@Composable
fun GbaRealisticConsole(
  gameState: GameStateSnapshot,
  showVisionOverlay: Boolean,
  customizationConfig: ConsoleCustomizationConfig = ConsoleCustomizationConfig(),
  grindBotTelemetry: GrindBotTelemetry? = null,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  onMenuPress: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val vibrator = remember {
    try { context.getSystemService(Vibrator::class.java) } catch (_: Exception) { null }
  }

  fun triggerHaptic(duration: Long = 10) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(duration)
      }
    } catch (_: Exception) {}
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .widthIn(max = 410.dp)
          .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // 1. TOP CLAMSHELL LID (SCREEN UNIT)
        TopLidScreenUnit(
          gameState = gameState,
          showVisionOverlay = showVisionOverlay,
          config = customizationConfig,
          grindBotTelemetry = grindBotTelemetry,
          modifier = Modifier
            .fillMaxWidth()
            .weight(1.02f)
        )

        // 2. CYLINDRICAL HINGE WITH L AND R SHOULDERS
        CylindricalHingeBar(
          config = customizationConfig,
          onKeyDown = { key -> triggerHaptic(14); onKeyDown(key) },
          onKeyUp = { key -> onKeyUp(key) },
          modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
        )

        // 3. LOWER CONTROLLER UNIT (BODY)
        LowerControllerBodyUnit(
          config = customizationConfig,
          onKeyDown = { key -> triggerHaptic(10); onKeyDown(key) },
          onKeyUp = { key -> onKeyUp(key) },
          onMenuPress = { triggerHaptic(22); onMenuPress() },
          modifier = Modifier
            .fillMaxWidth()
            .weight(1.18f)
        )
      }
    }
  }
}

/**
 * Top Clamshell Screen Unit: Nintendo logo, ownership badge, screen with black bezel, GBA SP logo
 */
@Composable
private fun TopLidScreenUnit(
  gameState: GameStateSnapshot,
  showVisionOverlay: Boolean,
  config: ConsoleCustomizationConfig,
  grindBotTelemetry: GrindBotTelemetry?,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            config.activeShellDeepColor,
            config.activeShellColor,
            config.activeShellHighlightColor,
            config.activeShellDeepColor
          )
        )
      )
      .border(
        width = 1.5.dp,
        brush = Brush.verticalGradient(
          colors = listOf(config.activeShellHighlightColor, Color(0xFF1E3A8A))
        ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
      )
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // 4 Corner Screw / Rubber Cushions
    CornerScrewCover(modifier = Modifier.align(Alignment.TopStart).padding(3.dp))
    CornerScrewCover(modifier = Modifier.align(Alignment.TopEnd).padding(3.dp))
    CornerScrewCover(modifier = Modifier.align(Alignment.BottomStart).padding(3.dp))
    CornerScrewCover(modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp))

    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header: Nintendo badge + Visible Input Ownership Badge
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        NintendoBadge()

        // Visible Control Ownership HUD: [HUMAN MANUAL] / [AI ACTIVE] / [PAUSED]
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(gameState.inputOwner.badgeColorHex))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = gameState.inputOwner.label,
            color = Color.White,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // GRIND BOT HUD BANNER IF ACTIVE
      if (grindBotTelemetry?.isEnabled == true) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(
              if (grindBotTelemetry.currentState == GrindBotState.SHINY_LOCKED)
                Color(0xFF854D0E)
              else
                Color(0xCC064E3B)
            )
            .border(
              0.5.dp,
              if (grindBotTelemetry.currentState == GrindBotState.SHINY_LOCKED) Color(0xFFFACC15) else Color(0xFF10B981),
              RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (grindBotTelemetry.currentState == GrindBotState.SHINY_LOCKED) "✨ SHINY LOCKED!" else "🤖 GRIND BOT ACTIVE",
              color = Color.White,
              fontSize = 7.5.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Encounters: ${grindBotTelemetry.totalEncounters} • Shinies: ${grindBotTelemetry.totalShiniesFound}",
              color = if (grindBotTelemetry.currentState == GrindBotState.SHINY_LOCKED) Color(0xFFFACC15) else Color(0xFF34D399),
              fontSize = 7.5.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(2.dp))

      // Obsidian Black Bezel framing the 3:2 GBA Screen
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF070A0F))
          .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
          .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
      ) {
        VirtualScreen(
          gameState = gameState,
          showVisionOverlay = showVisionOverlay,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // "GAME BOY ADVANCE SP" silver metallic text logo
      GbaAdvanceSpLogo()
    }
  }
}

/**
 * Cylindrical Hinge Bar connecting top lid and bottom body with holdable L & R triggers
 */
@Composable
private fun CylindricalHingeBar(
  config: ConsoleCustomizationConfig,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            config.activeShellDeepColor,
            config.activeShellColor,
            config.activeShellHighlightColor,
            config.activeShellDeepColor
          )
        )
      )
      .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp)),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    HoldableGbaShoulderTrigger(label = "L", key = GamepadKey.L, onKeyDown = onKeyDown, onKeyUp = onKeyUp, isLeft = true)

    // Center hinge seams (3-barrel look)
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.Center
    ) {
      Box(modifier = Modifier.width(2.5.dp).fillMaxHeight().background(Color(0xFF070B14)))
      Spacer(modifier = Modifier.width(42.dp))
      Box(modifier = Modifier.width(2.5.dp).fillMaxHeight().background(Color(0xFF070B14)))
    }

    HoldableGbaShoulderTrigger(label = "R", key = GamepadKey.R, onKeyDown = onKeyDown, onKeyUp = onKeyUp, isLeft = false)
  }
}

@Composable
private fun HoldableGbaShoulderTrigger(
  label: String,
  key: GamepadKey,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  isLeft: Boolean
) {
  var isPressed by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "trigger_scale")

  Box(
    modifier = Modifier
      .scale(scale)
      .width(58.dp)
      .fillMaxHeight()
      .clip(if (isLeft) RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp) else RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
      .background(
        Brush.horizontalGradient(
          colors = if (isLeft)
            listOf(Color(0xFF1E293B), Color(0xFF334155))
          else
            listOf(Color(0xFF334155), Color(0xFF1E293B))
        )
      )
      .pointerInput(key) {
        awaitPointerEventScope {
          while (true) {
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true
            onKeyDown(key)
            waitForUpOrCancellation()
            isPressed = false
            onKeyUp(key)
          }
        }
      }
      .testTag("btn_trigger_${key.name.lowercase()}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = if (isPressed) Color(0xFF38BDF8) else Color(0xFFE2E8F0),
      fontSize = 13.sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.SansSerif
    )
  }
}

/**
 * Lower Controller Body Unit: Menu button, Power LED, D-pad well, A/B well, speaker grille, Select/Start
 */
@Composable
private fun LowerControllerBodyUnit(
  config: ConsoleCustomizationConfig,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  onMenuPress: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp, topStart = 6.dp, topEnd = 6.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            config.activeShellHighlightColor,
            config.activeShellColor,
            config.activeShellDeepColor,
            Color(0xFF0F1E44)
          )
        )
      )
      .border(
        width = 1.5.dp,
        brush = Brush.verticalGradient(
          colors = listOf(config.activeShellHighlightColor, Color(0xFF172D6B))
        ),
        shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp, topStart = 6.dp, topEnd = 6.dp)
      )
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    CornerScrewCover(modifier = Modifier.align(Alignment.BottomStart).padding(3.dp))
    CornerScrewCover(modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp))

    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // 1. Top row of bottom body: Center circular MENU button and Right glowing Battery LED
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(34.dp)
      ) {
        BatteryStatusLed(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 6.dp)
        )

        MenuButtonWithLabel(
          onClick = onMenuPress,
          modifier = Modifier.align(Alignment.Center)
        )
      }

      // 2. Middle controls: D-Pad on Left, Speaker Grille in Center, B/A in angled well on Right
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left well: Circular concave well with D-Pad
        Box(
          modifier = Modifier
            .size(136.dp)
            .background(
              Brush.radialGradient(
                colors = listOf(config.activeShellDeepColor, Color(0xFF132857))
              ),
              CircleShape
            )
            .border(1.5.dp, Color(0xFF11234F), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          AuthenticHoldableDpadCross(config = config, onKeyDown = onKeyDown, onKeyUp = onKeyUp)
        }

        // Center: 3x4 Speaker Grille Holes
        SpeakerGrilleGrid()

        // Right well: Angled pill-shaped recess with B & A buttons
        Box(
          modifier = Modifier
            .width(136.dp)
            .height(82.dp)
            .rotate(-28f)
            .background(
              Brush.horizontalGradient(
                colors = listOf(config.activeShellDeepColor, Color(0xFF132857))
              ),
              RoundedCornerShape(41.dp)
            )
            .border(1.5.dp, Color(0xFF11234F), RoundedCornerShape(41.dp))
            .padding(horizontal = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            HoldableRoundButtonGba(
              label = "B",
              key = GamepadKey.B,
              customColor = if (config.isFamicomButtons) Color(0xFFEAB308) else config.activeButtonColor,
              customTextColor = config.activeButtonTextColor,
              onKeyDown = onKeyDown,
              onKeyUp = onKeyUp,
              rotation = 28f
            )
            HoldableRoundButtonGba(
              label = "A",
              key = GamepadKey.A,
              customColor = if (config.isFamicomButtons) Color(0xFFDC2626) else config.activeButtonColor,
              customTextColor = config.activeButtonTextColor,
              onKeyDown = onKeyDown,
              onKeyUp = onKeyUp,
              rotation = 28f
            )
          }
        }
      }

      // 3. Bottom Row: SELECT and START buttons in oval recesses
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("SELECT", color = SilkscreenSilver, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
          Spacer(modifier = Modifier.height(2.dp))
          HoldableOvalButtonGba(GamepadKey.SELECT, onKeyDown, onKeyUp)
        }

        Spacer(modifier = Modifier.width(42.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("START", color = SilkscreenSilver, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
          Spacer(modifier = Modifier.height(2.dp))
          HoldableOvalButtonGba(GamepadKey.START, onKeyDown, onKeyUp)
        }
      }
    }
  }
}

/**
 * Authentic D-Pad cross supporting simultaneous diagonal key combinations and held states
 */
@Composable
private fun AuthenticHoldableDpadCross(
  config: ConsoleCustomizationConfig,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit
) {
  val armWidth = 38.dp
  val totalSpan = 108.dp

  Box(
    modifier = Modifier.size(totalSpan),
    contentAlignment = Alignment.Center
  ) {
    // Horizontal Cross Arm
    Box(
      modifier = Modifier
        .width(totalSpan)
        .height(armWidth)
        .shadow(4.dp, RoundedCornerShape(4.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(Color(0xFF3B4455), config.activeDpadColor, Color(0xFF181D26))
          ),
          RoundedCornerShape(4.dp)
        )
        .border(0.5.dp, Color(0xFF4B5563), RoundedCornerShape(4.dp))
    )

    // Vertical Cross Arm
    Box(
      modifier = Modifier
        .width(armWidth)
        .height(totalSpan)
        .shadow(4.dp, RoundedCornerShape(4.dp))
        .background(
          Brush.horizontalGradient(
            colors = listOf(Color(0xFF3B4455), config.activeDpadColor, Color(0xFF181D26))
          ),
          RoundedCornerShape(4.dp)
        )
        .border(0.5.dp, Color(0xFF4B5563), RoundedCornerShape(4.dp))
    )

    // Center circular dimple
    Box(
      modifier = Modifier
        .size(22.dp)
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0xFF141922), Color(0xFF2B3342))
          ),
          CircleShape
        )
        .border(0.5.dp, Color(0xFF0A0E17), CircleShape)
    )

    // 4 Directional Touch Keys
    HoldableDpadKeyGba(Modifier.align(Alignment.TopCenter).size(armWidth), "▲", GamepadKey.UP, onKeyDown, onKeyUp)
    HoldableDpadKeyGba(Modifier.align(Alignment.BottomCenter).size(armWidth), "▼", GamepadKey.DOWN, onKeyDown, onKeyUp)
    HoldableDpadKeyGba(Modifier.align(Alignment.CenterStart).size(armWidth), "◀", GamepadKey.LEFT, onKeyDown, onKeyUp)
    HoldableDpadKeyGba(Modifier.align(Alignment.CenterEnd).size(armWidth), "▶", GamepadKey.RIGHT, onKeyDown, onKeyUp)
  }
}

@Composable
private fun HoldableDpadKeyGba(
  modifier: Modifier,
  symbol: String,
  key: GamepadKey,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit
) {
  var isPressed by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(if (isPressed) 0.90f else 1f, label = "dpad_scale")

  Box(
    modifier = modifier
      .scale(scale)
      .pointerInput(key) {
        awaitPointerEventScope {
          while (true) {
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true
            onKeyDown(key)
            waitForUpOrCancellation()
            isPressed = false
            onKeyUp(key)
          }
        }
      }
      .testTag("btn_dpad_${key.name.lowercase()}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = symbol,
      color = if (isPressed) Color(0xFF38BDF8) else Color(0xFF6B7280),
      fontSize = 11.sp,
      fontWeight = FontWeight.Black
    )
  }
}

@Composable
private fun HoldableRoundButtonGba(
  label: String,
  key: GamepadKey,
  customColor: Color = ButtonCharcoal,
  customTextColor: Color = Color(0xFFE2E8F0),
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  rotation: Float = 0f
) {
  var isPressed by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "btn_scale")

  Box(
    modifier = Modifier
      .scale(scale)
      .size(44.dp)
      .shadow(5.dp, CircleShape)
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF3E485A), customColor, Color(0xFF141822))
        ),
        CircleShape
      )
      .border(1.2.dp, Color(0xFF4B5563), CircleShape)
      .pointerInput(key) {
        awaitPointerEventScope {
          while (true) {
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true
            onKeyDown(key)
            waitForUpOrCancellation()
            isPressed = false
            onKeyUp(key)
          }
        }
      }
      .testTag("btn_action_${key.name.lowercase()}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = customTextColor,
      fontSize = 18.sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.SansSerif,
      modifier = Modifier.rotate(rotation)
    )
  }
}

@Composable
private fun HoldableOvalButtonGba(
  key: GamepadKey,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit
) {
  var isPressed by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(if (isPressed) 0.85f else 1f, label = "oval_scale")

  Box(
    modifier = Modifier
      .width(36.dp)
      .height(18.dp)
      .background(Color(0xFF0C1730), RoundedCornerShape(9.dp))
      .border(0.5.dp, Color(0xFF1A2B52), RoundedCornerShape(9.dp)),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .scale(scale)
        .width(30.dp)
        .height(12.dp)
        .shadow(2.dp, RoundedCornerShape(6.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(Color(0xFF334155), ButtonCharcoal)
          ),
          RoundedCornerShape(6.dp)
        )
        .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
        .pointerInput(key) {
          awaitPointerEventScope {
            while (true) {
              awaitFirstDown(requireUnconsumed = false)
              isPressed = true
              onKeyDown(key)
              waitForUpOrCancellation()
              isPressed = false
              onKeyUp(key)
            }
          }
        }
        .testTag("btn_${key.name.lowercase()}")
    )
  }
}

@Composable
private fun MenuButtonWithLabel(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isPressed by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "menu_btn_scale")

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .scale(scale)
        .size(24.dp)
        .shadow(3.dp, CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0xFF2A364F), Color(0xFF172033))
          ),
          CircleShape
        )
        .border(1.dp, Color(0xFF64748B), CircleShape)
        .pointerInput(Unit) {
          awaitPointerEventScope {
            while (true) {
              awaitFirstDown(requireUnconsumed = false)
              isPressed = true
              onClick()
              waitForUpOrCancellation()
              isPressed = false
            }
          }
        }
        .testTag("btn_console_menu"),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .background(Color(0xFF334155), CircleShape)
      )
    }

    Text(
      text = "MENU",
      color = SilkscreenSilver,
      fontSize = 7.5.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.SansSerif,
      letterSpacing = 0.5.sp
    )
  }
}

@Composable
private fun BatteryStatusLed(modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "led_bloom")
  val bloomAlpha by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "bloom"
  )

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .width(10.dp)
        .height(14.dp)
        .background(Color(0xFF1E293B), RoundedCornerShape(2.dp))
        .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(2.dp)),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .background(LedActiveGreen.copy(alpha = bloomAlpha), CircleShape)
          .shadow(6.dp, CircleShape, spotColor = LedActiveGreen)
      )
    }
  }
}

@Composable
private fun SpeakerGrilleGrid() {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.5.dp)
  ) {
    for (row in 0..3) {
      Row(horizontalArrangement = Arrangement.spacedBy(5.5.dp)) {
        for (col in 0..2) {
          Box(
            modifier = Modifier
              .size(4.dp)
              .background(Color(0xFF080E1C), CircleShape)
              .border(0.3.dp, Color(0xFF04070F), CircleShape)
          )
        }
      }
    }
  }
}

@Composable
private fun NintendoBadge() {
  Box(
    modifier = Modifier
      .border(1.2.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
      .padding(horizontal = 14.dp, vertical = 2.dp)
  ) {
    Text(
      text = "Nintendo",
      color = Color(0xFFF1F5F9),
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.SansSerif,
      letterSpacing = 1.sp
    )
  }
}

@Composable
private fun GbaAdvanceSpLogo() {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    Text(
      text = "GAME BOY",
      color = SilkscreenSilver,
      fontSize = 11.5.sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.SansSerif,
      letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = "ADVANCE",
      color = SilkscreenSilver,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.SansSerif,
      letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.width(4.dp))
    Box(
      modifier = Modifier
        .background(Color(0xFF334155), RoundedCornerShape(2.dp))
        .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
      Text(
        text = "SP",
        color = Color(0xFF38BDF8),
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.SansSerif
      )
    }
  }
}

@Composable
private fun CornerScrewCover(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(7.dp)
      .background(Color(0xFF1E293B), CircleShape)
      .border(0.5.dp, Color(0xFF0F172A), CircleShape)
  )
}
