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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameStateSnapshot
import com.example.model.GamepadKey

// Cobalt Metallic Blue Palette exactly matching the physical GBA SP photo
private val CobaltShellOuter = Color(0xFF142B66)
private val CobaltShellMain = Color(0xFF1A3B8B)
private val CobaltShellBright = Color(0xFF244CA9)
private val CobaltShellHighlight = Color(0xFF3362C9)
private val CobaltDeepWell = Color(0xFF0C1B3F)
private val ScrewCapRubber = Color(0xFF1E293B)
private val DpadCharcoal = Color(0xFF29303D)
private val ButtonCharcoal = Color(0xFF232936)
private val SilkscreenSilver = Color(0xFFCBD5E1)
private val LedActiveGreen = Color(0xFF22C55E)

@Composable
fun GbaRealisticConsole(
  gameState: GameStateSnapshot,
  showVisionOverlay: Boolean,
  onKeyPress: (GamepadKey) -> Unit,
  onMenuPress: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val vibrator = remember {
    try {
      context.getSystemService(Vibrator::class.java)
    } catch (_: Exception) {
      null
    }
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
      // Responsive handheld containment: maintains authentic clamshell vertical proportions
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
          modifier = Modifier
            .fillMaxWidth()
            .weight(1.02f)
        )

        // 2. CYLINDRICAL HINGE WITH L AND R SHOULDERS
        CylindricalHingeBar(
          onLPress = {
            triggerHaptic(14)
            onKeyPress(GamepadKey.L)
          },
          onRPress = {
            triggerHaptic(14)
            onKeyPress(GamepadKey.R)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
        )

        // 3. LOWER CONTROLLER UNIT (BODY)
        LowerControllerBodyUnit(
          onKeyPress = { key ->
            triggerHaptic(10)
            onKeyPress(key)
          },
          onMenuPress = {
            triggerHaptic(22)
            onMenuPress()
          },
          modifier = Modifier
            .fillMaxWidth()
            .weight(1.18f)
        )
      }
    }
  }
}

/**
 * Top Clamshell Screen Unit: Nintendo logo, screen with black bezel, GAME BOY ADVANCE SP logo, screw rubber covers
 */
@Composable
private fun TopLidScreenUnit(
  gameState: GameStateSnapshot,
  showVisionOverlay: Boolean,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(CobaltShellOuter, CobaltShellBright, CobaltShellMain, CobaltShellOuter)
        )
      )
      .border(
        width = 1.5.dp,
        brush = Brush.verticalGradient(
          colors = listOf(CobaltShellHighlight, Color(0xFF1E3A8A))
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
      // Nintendo oval badge
      NintendoBadge()

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
 * Cylindrical Hinge Bar connecting top lid and bottom body with L & R triggers
 */
@Composable
private fun CylindricalHingeBar(
  onLPress: () -> Unit,
  onRPress: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF09142C), CobaltShellOuter, CobaltShellBright, CobaltShellOuter, Color(0xFF09142C))
        )
      )
      .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp)),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left shoulder trigger L
    ShoulderTriggerKey(
      label = "L",
      onClick = onLPress,
      isLeft = true,
      testTag = "btn_trigger_l"
    )

    // Center hinge seams (3-barrel look)
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .width(2.5.dp)
          .fillMaxHeight()
          .background(Color(0xFF070B14))
      )
      Spacer(modifier = Modifier.width(42.dp))
      Box(
        modifier = Modifier
          .width(2.5.dp)
          .fillMaxHeight()
          .background(Color(0xFF070B14))
      )
    }

    // Right shoulder trigger R
    ShoulderTriggerKey(
      label = "R",
      onClick = onRPress,
      isLeft = false,
      testTag = "btn_trigger_r"
    )
  }
}

@Composable
private fun ShoulderTriggerKey(
  label: String,
  onClick: () -> Unit,
  isLeft: Boolean,
  testTag: String
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
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
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .testTag(testTag),
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
  onKeyPress: (GamepadKey) -> Unit,
  onMenuPress: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp, topStart = 6.dp, topEnd = 6.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(CobaltShellOuter, CobaltShellBright, CobaltShellMain, Color(0xFF0F1E44))
        )
      )
      .border(
        width = 1.5.dp,
        brush = Brush.verticalGradient(
          colors = listOf(CobaltShellHighlight, Color(0xFF172D6B))
        ),
        shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp, topStart = 6.dp, topEnd = 6.dp)
      )
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    // Bottom corner screws
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
        // Glowing Power LED on the top-right corner
        BatteryStatusLed(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 6.dp)
        )

        // Center circular MENU Button with metallic outer ring
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
                colors = listOf(CobaltDeepWell, Color(0xFF132857))
              ),
              CircleShape
            )
            .border(1.5.dp, Color(0xFF11234F), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          AuthenticDpadCross(onPress = onKeyPress)
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
                colors = listOf(CobaltDeepWell, Color(0xFF132857))
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
            // Button B (left-lower)
            AuthenticRoundButton(
              label = "B",
              onClick = { onKeyPress(GamepadKey.B) },
              testTag = "btn_action_b",
              rotation = 28f
            )

            // Button A (right-upper)
            AuthenticRoundButton(
              label = "A",
              onClick = { onKeyPress(GamepadKey.A) },
              testTag = "btn_action_a",
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
        // SELECT button
        Column(
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "SELECT",
            color = SilkscreenSilver,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          AuthenticOvalButton(
            onClick = { onKeyPress(GamepadKey.SELECT) },
            testTag = "btn_select"
          )
        }

        Spacer(modifier = Modifier.width(42.dp))

        // START button
        Column(
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "START",
            color = SilkscreenSilver,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          AuthenticOvalButton(
            onClick = { onKeyPress(GamepadKey.START) },
            testTag = "btn_start"
          )
        }
      }
    }
  }
}

/**
 * Authentic D-Pad cross with embossed directional triangles & 3D bevels
 */
@Composable
private fun AuthenticDpadCross(
  onPress: (GamepadKey) -> Unit
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
            colors = listOf(Color(0xFF3B4455), DpadCharcoal, Color(0xFF181D26))
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
            colors = listOf(Color(0xFF3B4455), DpadCharcoal, Color(0xFF181D26))
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

    // 4 Directional Touch Keys with embossed arrows
    DpadDirectionTouchKey(
      modifier = Modifier.align(Alignment.TopCenter).size(armWidth),
      symbol = "▲",
      onClick = { onPress(GamepadKey.UP) },
      testTag = "btn_dpad_up"
    )
    DpadDirectionTouchKey(
      modifier = Modifier.align(Alignment.BottomCenter).size(armWidth),
      symbol = "▼",
      onClick = { onPress(GamepadKey.DOWN) },
      testTag = "btn_dpad_down"
    )
    DpadDirectionTouchKey(
      modifier = Modifier.align(Alignment.CenterStart).size(armWidth),
      symbol = "◀",
      onClick = { onPress(GamepadKey.LEFT) },
      testTag = "btn_dpad_left"
    )
    DpadDirectionTouchKey(
      modifier = Modifier.align(Alignment.CenterEnd).size(armWidth),
      symbol = "▶",
      onClick = { onPress(GamepadKey.RIGHT) },
      testTag = "btn_dpad_right"
    )
  }
}

@Composable
private fun DpadDirectionTouchKey(
  modifier: Modifier,
  symbol: String,
  onClick: () -> Unit,
  testTag: String
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(if (isPressed) 0.90f else 1f, label = "dpad_scale")

  Box(
    modifier = modifier
      .scale(scale)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .testTag(testTag),
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

/**
 * Authentic circular A or B action button
 */
@Composable
private fun AuthenticRoundButton(
  label: String,
  onClick: () -> Unit,
  testTag: String,
  rotation: Float = 0f
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "btn_scale")

  Box(
    modifier = Modifier
      .scale(scale)
      .size(44.dp)
      .shadow(5.dp, CircleShape)
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF3E485A), ButtonCharcoal, Color(0xFF141822))
        ),
        CircleShape
      )
      .border(1.2.dp, Color(0xFF4B5563), CircleShape)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = Color(0xFFE2E8F0),
      fontSize = 18.sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.SansSerif,
      modifier = Modifier.rotate(rotation)
    )
  }
}

/**
 * Authentic oval SELECT / START button in recessed housing
 */
@Composable
private fun AuthenticOvalButton(
  onClick: () -> Unit,
  testTag: String
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
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
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick
        )
        .testTag(testTag)
    )
  }
}

/**
 * Center circular MENU button with metallic ring & text
 */
@Composable
private fun MenuButtonWithLabel(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
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
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick
        )
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

/**
 * Upper-right Power / Battery Status LED with glowing bloom
 */
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

/**
 * Exact 3x4 Speaker Grille Hole Pattern
 */
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

/**
 * Nintendo logo badge in oval border
 */
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

/**
 * "GAME BOY ADVANCE SP" logo below screen
 */
@Composable
private fun GbaAdvanceSpLogo() {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    Text(
      text = "GAME BOY ",
      color = Color(0xFFE2E8F0),
      fontSize = 11.sp,
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp
    )
    Text(
      text = "ADVANCE ",
      color = Color(0xFFCBD5E1),
      fontSize = 11.sp,
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Black,
      fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
      letterSpacing = 0.5.sp
    )
    Text(
      text = "SP",
      color = Color(0xFFF1F5F9),
      fontSize = 12.sp,
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Black,
      letterSpacing = 1.sp
    )
  }
}

/**
 * Corner rubber screw cover
 */
@Composable
private fun CornerScrewCover(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(11.dp)
      .background(Color(0xFF0B1426), CircleShape)
      .border(0.5.dp, Color(0xFF1E293B), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .size(7.dp)
        .background(ScrewCapRubber, CircleShape)
    )
  }
}
