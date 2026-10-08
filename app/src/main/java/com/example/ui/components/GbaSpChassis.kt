package com.example.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.model.GameConsoleMode
import com.example.model.GamepadKey
import com.example.ui.theme.CobaltCard
import com.example.ui.theme.CobaltShell
import com.example.ui.theme.CobaltShellShadow
import com.example.ui.theme.GamepadBtnA
import com.example.ui.theme.GamepadBtnB
import com.example.ui.theme.GamepadBtnX
import com.example.ui.theme.GamepadBtnY
import com.example.ui.theme.GamepadDpad
import com.example.ui.theme.GamepadStartSelect
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary

@Composable
fun GbaSpChassis(
  consoleMode: GameConsoleMode,
  onKeyPress: (GamepadKey) -> Unit,
  onToggleVisionOverlay: () -> Unit,
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

  fun triggerHaptic() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(12)
      }
    } catch (_: Exception) {}
  }

  // Cobalt Blue Metallic Clamshell lower unit (matching the user's sketch)
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 12.dp, topEnd = 12.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            CobaltShell,
            CobaltShellShadow,
            Color(0xFF0F172A)
          )
        )
      )
      .border(2.dp, Color(0xFF2563EB), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 12.dp, topEnd = 12.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Hinge & Shoulder Triggers Row (L and R buttons)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // L Trigger
      ShoulderButton(
        label = "L",
        onClick = {
          triggerHaptic()
          onKeyPress(GamepadKey.L)
        },
        testTag = "btn_trigger_l"
      )

      // Center Hinge Accent & Power/AI Status LED
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(NeonCyan, CircleShape)
            .border(1.dp, Color.White, CircleShape)
        )
        Text(
          text = if (consoleMode == GameConsoleMode.GBA) "GAME BOY ADVANCE SP" else "NINTENDO 3DS XL",
          color = Color(0xFFE2E8F0),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.SansSerif,
          letterSpacing = 1.sp
        )
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(Color(0xFF22C55E), CircleShape)
            .border(1.dp, Color.White, CircleShape)
        )
      }

      // R Trigger
      ShoulderButton(
        label = "R",
        onClick = {
          triggerHaptic()
          onKeyPress(GamepadKey.R)
        },
        testTag = "btn_trigger_r"
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Middle Partition: D-Pad (Left), Speaker/Menu (Center), Action Buttons (Right)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // LEFT SIDE: Directional D-Pad
      DirectionalDpad(
        onPress = { key ->
          triggerHaptic()
          onKeyPress(key)
        }
      )

      // CENTER: Speaker Grille & Screen Light/CV Overlay Toggle Button
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Speaker Grille Holes (2x3 pattern like real SP)
        Column(
          verticalArrangement = Arrangement.spacedBy(3.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SpeakerDot(); SpeakerDot()
          }
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SpeakerDot(); SpeakerDot(); SpeakerDot()
          }
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SpeakerDot(); SpeakerDot()
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center Button: Screen Overlay Toggle / Menu
        Box(
          modifier = Modifier
            .size(30.dp)
            .shadow(2.dp, CircleShape)
            .background(Color(0xFF1E293B), CircleShape)
            .border(1.dp, Color(0xFF64748B), CircleShape)
            .clickable {
              triggerHaptic()
              onToggleVisionOverlay()
            }
            .testTag("btn_menu_cv_toggle"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "AI/CV",
            color = NeonCyan,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
        Text(
          text = "OVERLAY",
          color = Color(0xFF94A3B8),
          fontSize = 7.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      // RIGHT SIDE: Action Buttons (A, B for GBA; plus X, Y for 3DS)
      if (consoleMode == GameConsoleMode.GBA) {
        GbaActionButtons(
          onPress = { key ->
            triggerHaptic()
            onKeyPress(key)
          }
        )
      } else {
        Nds3dsActionButtons(
          onPress = { key ->
            triggerHaptic()
            onKeyPress(key)
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // BOTTOM PARTITION: Select and Start oval buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OvalControlKey(
          label = "SELECT",
          onClick = {
            triggerHaptic()
            onKeyPress(GamepadKey.SELECT)
          },
          testTag = "btn_select"
        )
        Text("SELECT", color = Color(0xFF94A3B8), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
      }

      Spacer(modifier = Modifier.width(36.dp))

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OvalControlKey(
          label = "START",
          onClick = {
            triggerHaptic()
            onKeyPress(GamepadKey.START)
          },
          testTag = "btn_start"
        )
        Text("START", color = Color(0xFF94A3B8), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
      }
    }
  }
}

@Composable
private fun DirectionalDpad(
  onPress: (GamepadKey) -> Unit
) {
  val dpadSize = 120.dp
  val armSize = 40.dp

  Box(
    modifier = Modifier
      .size(dpadSize)
      .shadow(4.dp, RoundedCornerShape(12.dp))
      .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
      .padding(4.dp),
    contentAlignment = Alignment.Center
  ) {
    // Horizontal bar
    Box(
      modifier = Modifier
        .width(dpadSize - 8.dp)
        .height(armSize)
        .background(GamepadDpad, RoundedCornerShape(6.dp))
        .border(1.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
    )

    // Vertical bar
    Box(
      modifier = Modifier
        .width(armSize)
        .height(dpadSize - 8.dp)
        .background(GamepadDpad, RoundedCornerShape(6.dp))
        .border(1.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
    )

    // Center pivot
    Box(
      modifier = Modifier
        .size(24.dp)
        .background(Color(0xFF1E293B), CircleShape)
    )

    // Up Button
    DpadTouchZone(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .size(armSize),
      arrow = "▲",
      onClick = { onPress(GamepadKey.UP) },
      testTag = "btn_dpad_up"
    )

    // Down Button
    DpadTouchZone(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .size(armSize),
      arrow = "▼",
      onClick = { onPress(GamepadKey.DOWN) },
      testTag = "btn_dpad_down"
    )

    // Left Button
    DpadTouchZone(
      modifier = Modifier
        .align(Alignment.CenterStart)
        .size(armSize),
      arrow = "◀",
      onClick = { onPress(GamepadKey.LEFT) },
      testTag = "btn_dpad_left"
    )

    // Right Button
    DpadTouchZone(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .size(armSize),
      arrow = "▶",
      onClick = { onPress(GamepadKey.RIGHT) },
      testTag = "btn_dpad_right"
    )
  }
}

@Composable
private fun DpadTouchZone(
  modifier: Modifier,
  arrow: String,
  onClick: () -> Unit,
  testTag: String
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(if (isPressed) 0.90f else 1f, label = "press_scale")

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
      text = arrow,
      color = if (isPressed) NeonCyan else Color(0xFFCBD5E1),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun GbaActionButtons(
  onPress: (GamepadKey) -> Unit
) {
  // GBA angled B (left-lower) and A (right-higher)
  Row(
    modifier = Modifier
      .width(115.dp)
      .height(105.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // B Button (Lower)
    Column(
      modifier = Modifier.offset(y = 18.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      RoundActionButton(
        label = "B",
        buttonColor = GamepadBtnB,
        onClick = { onPress(GamepadKey.B) },
        testTag = "btn_action_b"
      )
    }

    // A Button (Higher)
    Column(
      modifier = Modifier.offset(y = (-10).dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      RoundActionButton(
        label = "A",
        buttonColor = GamepadBtnA,
        onClick = { onPress(GamepadKey.A) },
        testTag = "btn_action_a"
      )
    }
  }
}

@Composable
private fun Nds3dsActionButtons(
  onPress: (GamepadKey) -> Unit
) {
  // 3DS Diamond Layout: X (top), Y (left), A (right), B (bottom)
  Box(
    modifier = Modifier
      .size(115.dp)
      .padding(4.dp),
    contentAlignment = Alignment.Center
  ) {
    // X Button (Top)
    Box(modifier = Modifier.align(Alignment.TopCenter)) {
      RoundActionButton("X", GamepadBtnX, sizeDp = 34, onClick = { onPress(GamepadKey.X) }, testTag = "btn_action_x")
    }
    // Y Button (Left)
    Box(modifier = Modifier.align(Alignment.CenterStart)) {
      RoundActionButton("Y", GamepadBtnY, sizeDp = 34, onClick = { onPress(GamepadKey.Y) }, testTag = "btn_action_y")
    }
    // A Button (Right)
    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
      RoundActionButton("A", GamepadBtnA, sizeDp = 34, onClick = { onPress(GamepadKey.A) }, testTag = "btn_action_a")
    }
    // B Button (Bottom)
    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
      RoundActionButton("B", GamepadBtnB, sizeDp = 34, onClick = { onPress(GamepadKey.B) }, testTag = "btn_action_b")
    }
  }
}

@Composable
private fun RoundActionButton(
  label: String,
  buttonColor: Color,
  sizeDp: Int = 42,
  onClick: () -> Unit,
  testTag: String
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "btn_scale")

  Box(
    modifier = Modifier
      .scale(scale)
      .size(sizeDp.dp)
      .shadow(4.dp, CircleShape)
      .background(buttonColor, CircleShape)
      .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
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
      color = Color.White,
      fontSize = (sizeDp * 0.4).sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.SansSerif
    )
  }
}

@Composable
private fun ShoulderButton(
  label: String,
  onClick: () -> Unit,
  testTag: String
) {
  Box(
    modifier = Modifier
      .width(64.dp)
      .height(28.dp)
      .shadow(2.dp, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
      .background(Color(0xFF334155), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
      .border(1.dp, Color(0xFF64748B), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = Color(0xFFE2E8F0),
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.SansSerif
    )
  }
}

@Composable
private fun OvalControlKey(
  label: String,
  onClick: () -> Unit,
  testTag: String
) {
  Box(
    modifier = Modifier
      .width(36.dp)
      .height(14.dp)
      .shadow(2.dp, RoundedCornerShape(8.dp))
      .background(GamepadStartSelect, RoundedCornerShape(8.dp))
      .border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
  )
}

@Composable
private fun SpeakerDot() {
  Box(
    modifier = Modifier
      .size(4.dp)
      .background(Color(0xFF0F172A), CircleShape)
  )
}
