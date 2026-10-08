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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConsoleCustomizationConfig
import com.example.model.GamepadKey
import com.example.model.GameStateSnapshot
import com.example.model.GrindBotState
import com.example.model.GrindBotTelemetry
import kotlin.math.roundToInt

// Authentic Nintendo 3DS Cosmos Black & Metallic Slate Blue Palette
private val NdsChassisDark = Color(0xFF0F172A)
private val NdsChassisMain = Color(0xFF1E293B)
private val NdsChassisHighlight = Color(0xFF334155)
private val NdsInnerBezel = Color(0xFF020617)
private val NdsCirclePadGrip = Color(0xFF475569)
private val NdsButtonFace = Color(0xFF1E293B)
private val NdsLedActive = Color(0xFF10B981)
private val Nds3dLed = Color(0xFF06B6D4)

/**
 * Authentic Nintendo 3DS Dual-Screen Console Chassis.
 * Provides a 5:3 stereoscopic top display, a 4:3 resistive bottom touch screen with stylus digitizer,
 * analog Circle Pad, X/Y/B/A diamond buttons, ZL/ZR triggers, and HOME button.
 */
@Composable
fun Nds3dsRealisticConsole(
  gameState: GameStateSnapshot,
  showVisionOverlay: Boolean,
  customizationConfig: ConsoleCustomizationConfig = ConsoleCustomizationConfig(),
  grindBotTelemetry: GrindBotTelemetry? = null,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  onCirclePad: (Float, Float) -> Unit,
  onTouchDown: (Float, Float) -> Unit,
  onTouchUp: () -> Unit,
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
        .padding(horizontal = 6.dp, vertical = 4.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .widthIn(max = 412.dp)
          .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // -------------------------------------------------------------
        // 1. TOP 3DS UNIT: Widescreen 5:3 Display + 3D Depth Slider + Stereo Speakers
        // -------------------------------------------------------------
        Top3dsLidUnit(
          gameState = gameState,
          showVisionOverlay = showVisionOverlay,
          config = customizationConfig,
          grindBotTelemetry = grindBotTelemetry,
          modifier = Modifier
            .fillMaxWidth()
            .weight(1.05f)
        )

        // -------------------------------------------------------------
        // 2. CYLINDRICAL HINGE WITH L, ZL, R, ZR SHOULDERS
        // -------------------------------------------------------------
        HingeWithExtendedTriggers(
          onKeyDown = { key -> triggerHaptic(12); onKeyDown(key) },
          onKeyUp = { key -> onKeyUp(key) },
          modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
        )

        // -------------------------------------------------------------
        // 3. BOTTOM 3DS UNIT: Touch Screen, Circle Pad, D-Pad, ABXY, Home
        // -------------------------------------------------------------
        Bottom3dsBodyUnit(
          gameState = gameState,
          config = customizationConfig,
          onKeyDown = { key -> triggerHaptic(10); onKeyDown(key) },
          onKeyUp = { key -> onKeyUp(key) },
          onCirclePad = onCirclePad,
          onTouchDown = { x, y -> triggerHaptic(8); onTouchDown(x, y) },
          onTouchUp = onTouchUp,
          onHomePress = { triggerHaptic(20); onMenuPress() },
          modifier = Modifier
            .fillMaxWidth()
            .weight(1.22f)
        )
      }
    }
  }
}

/**
 * Top 3DS Clamshell Unit (5:3 Widescreen 3D Display, Camera lens, Stereo Speakers, 3D Slider)
 */
@Composable
private fun Top3dsLidUnit(
  gameState: GameStateSnapshot,
  showVisionOverlay: Boolean,
  config: ConsoleCustomizationConfig,
  grindBotTelemetry: GrindBotTelemetry?,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
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
      .border(1.5.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Bezel Header: 3D Camera lens and Status LEDs
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left rubber bumper
        Box(modifier = Modifier.size(6.dp, 3.dp).background(Color(0xFF334155), RoundedCornerShape(1.dp)))

        // Inner 3D Camera lens
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(Color(0xFF020617), CircleShape)
            .border(0.5.dp, Color(0xFF64748B), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Box(modifier = Modifier.size(3.dp).background(Color(0xFF1E3A8A), CircleShape))
        }

        // Right rubber bumper
        Box(modifier = Modifier.size(6.dp, 3.dp).background(Color(0xFF334155), RoundedCornerShape(1.dp)))
      }

      // GRIND BOT HUD BANNER IF ACTIVE ON 3DS
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
              text = if (grindBotTelemetry.currentState == GrindBotState.SHINY_LOCKED) "✨ SHINY DETECTED!" else "🤖 BOT: ${grindBotTelemetry.goal.displayName.take(14)}",
              color = Color.White,
              fontSize = 7.5.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Enc: ${grindBotTelemetry.totalEncounters} • ✨ ${grindBotTelemetry.totalShiniesFound}",
              color = if (grindBotTelemetry.currentState == GrindBotState.SHINY_LOCKED) Color(0xFFFACC15) else Color(0xFF34D399),
              fontSize = 7.5.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      // Middle Row: Left Speaker, Top Widescreen, Right Speaker & 3D Depth Slider
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left Stereo Speaker grille pattern
        NdsSpeakerGrille()

        // 5:3 Native Top 3D Widescreen Display
        Box(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 6.dp)
            .aspectRatio(1.66f) // 5:3 400x240 native
            .clip(RoundedCornerShape(4.dp))
            .background(NdsInnerBezel)
            .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(4.dp))
        ) {
          VirtualScreen(
            gameState = gameState,
            showVisionOverlay = showVisionOverlay,
            modifier = Modifier.fillMaxSize()
          )

          // 3D Depth Active Indicator Badge
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(4.dp)
              .background(Color(0x99000000), RoundedCornerShape(3.dp))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(5.dp).background(Nds3dLed, CircleShape))
              Spacer(modifier = Modifier.width(3.dp))
              Text("3D", color = Nds3dLed, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // Right side: Stereo speaker + Physical 3D Depth Slider
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          NdsSpeakerGrille()
          Spacer(modifier = Modifier.height(8.dp))
          Depth3dSlider()
        }
      }

      // Bottom bezel logo
      Text(
        text = "Nintendo 3DS",
        color = Color(0xFF94A3B8),
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = 1.sp
      )
    }
  }
}

/**
 * 3DS Hinge Bar with L, ZL, R, ZR Triggers
 */
@Composable
private fun HingeWithExtendedTriggers(
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF090D16), Color(0xFF1E293B), Color(0xFF090D16))
        )
      )
      .border(1.dp, Color(0xFF334155), RoundedCornerShape(4.dp)),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left Triggers (L and ZL)
    Row {
      HoldableTriggerKey(label = "ZL", key = GamepadKey.ZL, onKeyDown = onKeyDown, onKeyUp = onKeyUp, width = 36)
      Spacer(modifier = Modifier.width(2.dp))
      HoldableTriggerKey(label = "L", key = GamepadKey.L, onKeyDown = onKeyDown, onKeyUp = onKeyUp, width = 46)
    }

    // Center Hinge Seam & Notification LED
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(modifier = Modifier.width(1.5.dp).height(18.dp).background(Color(0xFF0B132B)))
      Spacer(modifier = Modifier.width(24.dp))
      Box(
        modifier = Modifier
          .size(6.dp)
          .background(Color(0xFF38BDF8), CircleShape)
          .shadow(4.dp, CircleShape, spotColor = Color(0xFF38BDF8))
      )
      Spacer(modifier = Modifier.width(24.dp))
      Box(modifier = Modifier.width(1.5.dp).height(18.dp).background(Color(0xFF0B132B)))
    }

    // Right Triggers (R and ZR)
    Row {
      HoldableTriggerKey(label = "R", key = GamepadKey.R, onKeyDown = onKeyDown, onKeyUp = onKeyUp, width = 46)
      Spacer(modifier = Modifier.width(2.dp))
      HoldableTriggerKey(label = "ZR", key = GamepadKey.ZR, onKeyDown = onKeyDown, onKeyUp = onKeyUp, width = 36)
    }
  }
}

/**
 * Bottom 3DS Unit: Touch Screen, Circle Pad, D-Pad, Diamond ABXY, Home Button
 */
@Composable
private fun Bottom3dsBodyUnit(
  gameState: GameStateSnapshot,
  config: ConsoleCustomizationConfig,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  onCirclePad: (Float, Float) -> Unit,
  onTouchDown: (Float, Float) -> Unit,
  onTouchUp: () -> Unit,
  onHomePress: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp, topStart = 6.dp, topEnd = 6.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            config.activeShellHighlightColor,
            config.activeShellColor,
            config.activeShellDeepColor,
            Color(0xFF0A0F1D)
          )
        )
      )
      .border(1.5.dp, Color(0xFF1E3A8A), RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp, topStart = 6.dp, topEnd = 6.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Main Center Row: Left Controls (Circle Pad + D-Pad), Center Touch Screen, Right ABXY
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left Column: Circle Pad (above) and D-Pad (below)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.width(100.dp).fillMaxHeight()
        ) {
          // Analog Circle Pad
          InteractiveCirclePad(onMove = onCirclePad)

          // 3DS Tactile D-Pad
          NdsTactileDpad(config = config, onKeyDown = onKeyDown, onKeyUp = onKeyUp)
        }

        // Center: 4:3 Resistive Touch Screen (320x240) with touch digitizer
        Box(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 6.dp)
            .aspectRatio(1.33f) // 4:3 touch display
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF030712))
            .border(2.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
        ) {
          NdsResistiveTouchScreen(
            gameState = gameState,
            onTouchDown = onTouchDown,
            onTouchUp = onTouchUp,
            modifier = Modifier.fillMaxSize()
          )
        }

        // Right Column: ABXY Diamond Buttons and Power indicator
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.width(100.dp).fillMaxHeight()
        ) {
          // Diamond ABXY Buttons
          NdsDiamondActionButtons(config = config, onKeyDown = onKeyDown, onKeyUp = onKeyUp)

          // START & SELECT buttons
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NdsSmallPillButton("SELECT", GamepadKey.SELECT, onKeyDown, onKeyUp)
            NdsSmallPillButton("START", GamepadKey.START, onKeyDown, onKeyUp)
          }
        }
      }

      // Bottom Row: Centered HOME button, Mic, and Power LED
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(26.dp)
          .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Mic hole
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(3.dp).background(Color(0xFF334155), CircleShape))
          Spacer(modifier = Modifier.width(4.dp))
          Text("MIC", color = Color(0xFF64748B), fontSize = 7.sp, fontFamily = FontFamily.SansSerif)
        }

        // 3DS HOME Button (Pauses or opens EdgePilot Menu)
        Box(
          modifier = Modifier
            .width(52.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(
              Brush.verticalGradient(
                colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
              )
            )
            .border(1.dp, Color(0xFF64748B), RoundedCornerShape(9.dp))
            .pointerInput(Unit) {
              awaitPointerEventScope {
                while (true) {
                  awaitFirstDown(requireUnconsumed = false)
                  onHomePress()
                  waitForUpOrCancellation()
                }
              }
            }
            .testTag("btn_3ds_home"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "HOME",
            color = Color(0xFFF1F5F9),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        }

        // Glowing Blue Power LED
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(5.dp)
              .background(NdsLedActive, CircleShape)
              .shadow(4.dp, CircleShape, spotColor = NdsLedActive)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("POWER", color = Color(0xFF64748B), fontSize = 7.sp)
        }
      }
    }
  }
}

/**
 * Interactive Resistive Touch Screen for 3DS Bottom View
 */
@Composable
private fun NdsResistiveTouchScreen(
  gameState: GameStateSnapshot,
  onTouchDown: (Float, Float) -> Unit,
  onTouchUp: () -> Unit,
  modifier: Modifier = Modifier
) {
  var touchPos by remember { mutableStateOf<Offset?>(null) }

  Box(
    modifier = modifier
      .pointerInput(Unit) {
        awaitPointerEventScope {
          while (true) {
            val down = awaitFirstDown(requireUnconsumed = false)
            val w = size.width.toFloat()
            val h = size.height.toFloat()
            touchPos = down.position
            onTouchDown(down.position.x / w, down.position.y / h)

            // Track drag while finger is held
            while (true) {
              val event = awaitPointerEvent()
              val change = event.changes.firstOrNull()
              if (change == null || !change.pressed) {
                touchPos = null
                onTouchUp()
                break
              } else {
                touchPos = change.position
                onTouchDown(change.position.x / w, change.position.y / h)
              }
            }
          }
        }
      }
  ) {
    // Canvas drawing 3DS Bottom Screen UI
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // Background grid
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        )
      )

      // Stylus digitizer fine grid lines
      var gridY = 0f
      while (gridY < h) {
        drawLine(Color(0x18FFFFFF), Offset(0f, gridY), Offset(w, gridY), 0.5f)
        gridY += 8f
      }

      if (gameState.isInBattle) {
        // -------------------------------------------------------------
        // 3DS ROTOM BATTLE TOUCHPAD (4 QUADRANTS)
        // -------------------------------------------------------------
        val padW = (w - 12f) / 2f
        val padH = (h - 12f) / 2f

        // Top-Left: FIGHT (Red)
        drawRoundRect(Color(0xFFDC2626), Offset(4f, 4f), Size(padW, padH), CornerRadius(8f, 8f))
        // Top-Right: BAG (Yellow)
        drawRoundRect(Color(0xFFD97706), Offset(w - padW - 4f, 4f), Size(padW, padH), CornerRadius(8f, 8f))
        // Bottom-Left: POKEMON (Green)
        drawRoundRect(Color(0xFF16A34A), Offset(4f, h - padH - 4f), Size(padW, padH), CornerRadius(8f, 8f))
        // Bottom-Right: RUN (Blue)
        drawRoundRect(Color(0xFF2563EB), Offset(w - padW - 4f, h - padH - 4f), Size(padW, padH), CornerRadius(8f, 8f))

      } else {
        // -------------------------------------------------------------
        // OVERWORLD ROTOM MAP & TOUCH INVENTORY
        // -------------------------------------------------------------
        // Center mini-map radar
        drawCircle(
          color = Color(0x3338BDF8),
          radius = h * 0.35f,
          center = Offset(w * 0.5f, h * 0.5f)
        )
        drawCircle(
          color = Color(0xFF38BDF8),
          radius = 5f,
          center = Offset(w * 0.5f, h * 0.5f)
        )
        // Compass cross
        drawLine(Color(0x4438BDF8), Offset(w * 0.5f, h * 0.15f), Offset(w * 0.5f, h * 0.85f), 1f)
        drawLine(Color(0x4438BDF8), Offset(w * 0.25f, h * 0.5f), Offset(w * 0.75f, h * 0.5f), 1f)
      }

      // Visual Touch Ripple / Stylus crosshair feedback
      touchPos?.let { pos ->
        drawCircle(Color(0x6638BDF8), radius = 16f, center = pos)
        drawCircle(Color(0xFF38BDF8), radius = 5f, center = pos)
        drawLine(Color.White, Offset(pos.x - 10f, pos.y), Offset(pos.x + 10f, pos.y), 1.5f)
        drawLine(Color.White, Offset(pos.x, pos.y - 10f), Offset(pos.x, pos.y + 10f), 1.5f)
      }
    }

    // Touch Screen Text Labels
    if (gameState.isInBattle) {
      Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("⚔ FIGHT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
          Text("🎒 BAG", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("🔴 POKÉMON", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
          Text("🏃 RUN", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
      }
    } else {
      Box(
        modifier = Modifier.fillMaxSize().padding(6.dp),
        contentAlignment = Alignment.BottomCenter
      ) {
        Text(
          text = "TOUCH SCREEN: Tap map or target to interact",
          color = Color(0xFF94A3B8),
          fontSize = 8.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

/**
 * 3DS Analog Circle Pad with interactive 360-degree drag physics
 */
@Composable
private fun InteractiveCirclePad(
  onMove: (Float, Float) -> Unit
) {
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }

  Box(
    modifier = Modifier
      .size(54.dp)
      .background(Color(0xFF090D16), CircleShape)
      .border(1.5.dp, Color(0xFF1E293B), CircleShape)
      .pointerInput(Unit) {
        detectDragGestures(
          onDrag = { change, dragAmount ->
            change.consume()
            val maxDrag = 18f
            offsetX = (offsetX + dragAmount.x).coerceIn(-maxDrag, maxDrag)
            offsetY = (offsetY + dragAmount.y).coerceIn(-maxDrag, maxDrag)
            onMove(offsetX / maxDrag, offsetY / maxDrag)
          },
          onDragEnd = {
            offsetX = 0f
            offsetY = 0f
            onMove(0f, 0f)
          },
          onDragCancel = {
            offsetX = 0f
            offsetY = 0f
            onMove(0f, 0f)
          }
        )
      },
    contentAlignment = Alignment.Center
  ) {
    // Concave analog nub thumb grip with radial rubber ring
    Box(
      modifier = Modifier
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .size(42.dp)
        .shadow(4.dp, CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0xFF64748B), NdsCirclePadGrip, Color(0xFF1E293B))
          ),
          CircleShape
        )
        .border(1.2.dp, Color(0xFF94A3B8), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(18.dp)
          .border(1.dp, Color(0xFF334155), CircleShape)
      )
    }
  }
}

/**
 * 3DS Tactile D-Pad Cross with real KeyDown/KeyUp contract
 */
@Composable
private fun NdsTactileDpad(
  config: ConsoleCustomizationConfig,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit
) {
  val armW = 24.dp
  val span = 68.dp

  Box(
    modifier = Modifier.size(span),
    contentAlignment = Alignment.Center
  ) {
    // Cross background
    Box(
      modifier = Modifier
        .width(span)
        .height(armW)
        .background(config.activeDpadColor, RoundedCornerShape(3.dp))
        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(3.dp))
    )
    Box(
      modifier = Modifier
        .width(armW)
        .height(span)
        .background(config.activeDpadColor, RoundedCornerShape(3.dp))
        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(3.dp))
    )

    // 4 Directional Touch Zones
    HoldableDpadKey(Modifier.align(Alignment.TopCenter).size(armW), "▲", GamepadKey.UP, onKeyDown, onKeyUp)
    HoldableDpadKey(Modifier.align(Alignment.BottomCenter).size(armW), "▼", GamepadKey.DOWN, onKeyDown, onKeyUp)
    HoldableDpadKey(Modifier.align(Alignment.CenterStart).size(armW), "◀", GamepadKey.LEFT, onKeyDown, onKeyUp)
    HoldableDpadKey(Modifier.align(Alignment.CenterEnd).size(armW), "▶", GamepadKey.RIGHT, onKeyDown, onKeyUp)
  }
}

@Composable
private fun HoldableDpadKey(
  modifier: Modifier,
  symbol: String,
  key: GamepadKey,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit
) {
  var isPressed by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
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
      .testTag("btn_3ds_${key.name.lowercase()}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = symbol,
      color = if (isPressed) Color(0xFF38BDF8) else Color(0xFF94A3B8),
      fontSize = 9.sp,
      fontWeight = FontWeight.Black
    )
  }
}

/**
 * 3DS Diamond Action Buttons (X, Y, B, A) with real KeyDown/KeyUp contract
 */
@Composable
private fun NdsDiamondActionButtons(
  config: ConsoleCustomizationConfig,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit
) {
  val span = 84.dp
  val isFamicom = config.isFamicomButtons

  Box(
    modifier = Modifier.size(span),
    contentAlignment = Alignment.Center
  ) {
    // X (Top)
    HoldableRoundButton(
      label = "X",
      key = GamepadKey.X,
      color = if (isFamicom) Color(0xFF2563EB) else config.activeButtonColor,
      textColor = if (isFamicom) Color.White else config.activeButtonTextColor,
      onKeyDown = onKeyDown,
      onKeyUp = onKeyUp,
      modifier = Modifier.align(Alignment.TopCenter)
    )
    // Y (Left)
    HoldableRoundButton(
      label = "Y",
      key = GamepadKey.Y,
      color = if (isFamicom) Color(0xFF16A34A) else config.activeButtonColor,
      textColor = if (isFamicom) Color.White else config.activeButtonTextColor,
      onKeyDown = onKeyDown,
      onKeyUp = onKeyUp,
      modifier = Modifier.align(Alignment.CenterStart)
    )
    // A (Right)
    HoldableRoundButton(
      label = "A",
      key = GamepadKey.A,
      color = if (isFamicom) Color(0xFFDC2626) else config.activeButtonColor,
      textColor = if (isFamicom) Color.White else config.activeButtonTextColor,
      onKeyDown = onKeyDown,
      onKeyUp = onKeyUp,
      modifier = Modifier.align(Alignment.CenterEnd)
    )
    // B (Bottom)
    HoldableRoundButton(
      label = "B",
      key = GamepadKey.B,
      color = if (isFamicom) Color(0xFFEAB308) else config.activeButtonColor,
      textColor = if (isFamicom) Color.White else config.activeButtonTextColor,
      onKeyDown = onKeyDown,
      onKeyUp = onKeyUp,
      modifier = Modifier.align(Alignment.BottomCenter)
    )
  }
}

@Composable
private fun HoldableRoundButton(
  label: String,
  key: GamepadKey,
  color: Color,
  textColor: Color = Color.White,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  modifier: Modifier = Modifier
) {
  var isPressed by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "btn_scale")

  Box(
    modifier = modifier
      .size(26.dp)
      .shadow(3.dp, CircleShape)
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF334155), color, Color(0xFF0F172A))
        ),
        CircleShape
      )
      .border(1.dp, color.copy(alpha = 0.8f), CircleShape)
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
      .testTag("btn_3ds_${key.name.lowercase()}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.SansSerif
    )
  }
}

@Composable
private fun HoldableTriggerKey(
  label: String,
  key: GamepadKey,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit,
  width: Int
) {
  var isPressed by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .width(width.dp)
      .fillMaxHeight()
      .clip(RoundedCornerShape(3.dp))
      .background(
        Brush.verticalGradient(
          colors = if (isPressed) listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)) else listOf(Color(0xFF1E293B), Color(0xFF334155))
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
      .testTag("btn_3ds_${key.name.lowercase()}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = if (isPressed) Color.White else Color(0xFFE2E8F0),
      fontSize = 11.sp,
      fontWeight = FontWeight.Black
    )
  }
}

@Composable
private fun NdsSmallPillButton(
  label: String,
  key: GamepadKey,
  onKeyDown: (GamepadKey) -> Unit,
  onKeyUp: (GamepadKey) -> Unit
) {
  var isPressed by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .width(28.dp)
      .height(10.dp)
      .clip(RoundedCornerShape(5.dp))
      .background(if (isPressed) Color(0xFF38BDF8) else Color(0xFF1E293B))
      .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(5.dp))
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
      .testTag("btn_3ds_${key.name.lowercase()}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = Color(0xFFCBD5E1),
      fontSize = 6.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun NdsSpeakerGrille() {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    for (i in 0..2) {
      Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (j in 0..1) {
          Box(modifier = Modifier.size(3.dp).background(Color(0xFF020617), CircleShape))
        }
      }
    }
  }
}

@Composable
private fun Depth3dSlider() {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .width(6.dp)
        .height(30.dp)
        .background(Color(0xFF020617), RoundedCornerShape(3.dp))
        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(3.dp)),
      contentAlignment = Alignment.TopCenter
    ) {
      Box(
        modifier = Modifier
          .width(8.dp)
          .height(10.dp)
          .background(Color(0xFF64748B), RoundedCornerShape(2.dp))
      )
    }
    Text("3D", color = Color(0xFF64748B), fontSize = 7.sp, fontWeight = FontWeight.Bold)
  }
}
