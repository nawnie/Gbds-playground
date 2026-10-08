package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameConsoleMode
import com.example.model.GameStateSnapshot
import com.example.model.VisionCategory
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextPrimary

@Composable
fun VirtualScreen(
  gameState: GameStateSnapshot,
  showVisionOverlay: Boolean,
  modifier: Modifier = Modifier
) {
  val is3Ds = gameState.consoleMode == GameConsoleMode.NDS_3DS

  Box(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(if (is3Ds) 1.6f else 1.5f) // GBA native 3:2
      .clip(RoundedCornerShape(4.dp))
      .background(Color(0xFF0F172A))
  ) {
    // 1. Core Animated Game Display (Intro Checkered Screen, Overworld, or Battle)
    GameDisplayCanvas(gameState = gameState)

    // 2. Authentic GBA LCD Dot Matrix / Scanline Grid Mesh
    LcdPixelMeshOverlay()

    // 3. Computer Vision Overlay (if enabled)
    if (showVisionOverlay) {
      VisionOverlayCanvas(gameState = gameState)
    }

    // 4. Discreet In-Game Screen Subtitle / Status HUD (Top corners)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .background(Color(0x99000000), RoundedCornerShape(3.dp))
          .padding(horizontal = 4.dp, vertical = 1.5.dp)
      ) {
        Text(
          text = gameState.zoneName,
          color = Color(0xFFF1F5F9),
          fontSize = 8.5.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      Box(
        modifier = Modifier
          .background(Color(0x99000000), RoundedCornerShape(3.dp))
          .padding(horizontal = 4.dp, vertical = 1.5.dp)
      ) {
        Text(
          text = "60 FPS • DMA: ${gameState.inputBufferHex}",
          color = NeonCyan,
          fontSize = 8.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

/**
 * Animated Canvas drawing the game screen with pixel art textures
 */
@Composable
private fun GameDisplayCanvas(gameState: GameStateSnapshot) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 5f,
    animationSpec = infiniteRepeatable(
      animation = tween(700, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_val"
  )

  val blinkAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(500, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "blink_val"
  )

  // Check if we are in the intro dialogue state (like in the user's reference image!)
  val isIntroScreen = gameState.dialogText.contains("boy", ignoreCase = true) ||
    gameState.dialogText.contains("tell me", ignoreCase = true) ||
    gameState.dialogText.contains("oak", ignoreCase = true)

  Canvas(modifier = Modifier.fillMaxSize()) {
    val width = size.width
    val height = size.height

    if (isIntroScreen) {
      // -------------------------------------------------------------
      // EXACT INTRO SCREEN FROM REFERENCE IMAGE (POKEMON GBA INTRO)
      // -------------------------------------------------------------
      // Top and bottom dark green borders
      drawRect(
        color = Color(0xFF0F2B1D),
        topLeft = Offset(0f, 0f),
        size = Size(width, height * 0.14f)
      )
      drawRect(
        color = Color(0xFF0F2B1D),
        topLeft = Offset(0f, height * 0.86f),
        size = Size(width, height * 0.14f)
      )

      // Center teal-green pixel gradient field
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xFFE6F4EA),
            Color(0xFFCEEAD6),
            Color(0xFF81C995),
            Color(0xFF2E6B47)
          ),
          startY = height * 0.14f,
          endY = height * 0.86f
        ),
        topLeft = Offset(0f, height * 0.14f),
        size = Size(width, height * 0.72f)
      )

      // Subtle checkered dot-matrix grid
      val gridStep = 8f
      var y = height * 0.14f
      while (y < height * 0.86f) {
        var x = 0f
        while (x < width) {
          drawRect(
            color = Color(0x1A000000),
            topLeft = Offset(x, y),
            size = Size(4f, 4f)
          )
          x += gridStep
        }
        y += gridStep
      }

    } else if (gameState.isInBattle) {
      // -------------------------------------------------------------
      // BATTLE SCENE
      // -------------------------------------------------------------
      // Sky
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD))
        ),
        topLeft = Offset(0f, 0f),
        size = Size(width, height * 0.65f)
      )
      // Ground / grass
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF16A34A), Color(0xFF15803D))
        ),
        topLeft = Offset(0f, height * 0.65f),
        size = Size(width, height * 0.35f)
      )
      // Opponent grass battle circle
      drawOval(
        color = Color(0xFF22C55E),
        topLeft = Offset(width * 0.52f, height * 0.20f),
        size = Size(width * 0.44f, height * 0.18f)
      )
      // Player grass battle circle
      drawOval(
        color = Color(0xFF166534),
        topLeft = Offset(width * 0.06f, height * 0.60f),
        size = Size(width * 0.52f, height * 0.24f)
      )

      // Opponent Sprite placeholder (animated)
      val oppX = width * 0.74f
      val oppY = height * 0.22f + pulseAnim
      val oppColor = if (gameState.isEnemyShiny) Color(0xFFF59E0B) else Color(0xFFE11D48)

      // Golden sparkle stars if Shiny!
      if (gameState.isEnemyShiny) {
        drawCircle(
          color = Color(0xFFFACC15),
          radius = width * 0.095f,
          center = Offset(oppX, oppY),
          style = Stroke(width = 2.5f)
        )
        drawCircle(
          color = Color(0xFFFEF08A),
          radius = 5.5f,
          center = Offset(oppX - 26f + pulseAnim * 2, oppY - 22f)
        )
        drawCircle(
          color = Color(0xFFFEF08A),
          radius = 6.5f,
          center = Offset(oppX + 24f - pulseAnim * 2, oppY - 20f)
        )
        drawCircle(
          color = Color(0xFFFEF08A),
          radius = 5.0f,
          center = Offset(oppX + 18f, oppY + 24f + pulseAnim)
        )
      }

      drawCircle(
        color = oppColor,
        radius = width * 0.08f,
        center = Offset(oppX, oppY)
      )
      drawCircle(
        color = Color.White,
        radius = width * 0.02f,
        center = Offset(oppX - 6f, oppY - 4f)
      )

      // Player Back Sprite (Brendan / Red)
      val playerX = width * 0.28f
      val playerY = height * 0.68f
      drawCircle(
        color = Color(0xFF2563EB),
        radius = width * 0.10f,
        center = Offset(playerX, playerY)
      )
      drawCircle(
        color = Color(0xFFDC2626),
        radius = width * 0.055f,
        center = Offset(playerX, playerY - width * 0.065f)
      )

    } else {
      // -------------------------------------------------------------
      // OVERWORLD RETRO SCENE
      // -------------------------------------------------------------
      // Base grass
      drawRect(
        color = Color(0xFF4ADE80),
        topLeft = Offset(0f, 0f),
        size = Size(width, height)
      )
      // Paved road
      drawRect(
        color = Color(0xFFFDE68A),
        topLeft = Offset(width * 0.36f, 0f),
        size = Size(width * 0.28f, height)
      )

      // Trees along borders
      val treeRadius = width * 0.075f
      for (i in 0..5) {
        drawCircle(
          color = Color(0xFF15803D),
          radius = treeRadius,
          center = Offset(width * 0.1f, height * (i * 0.18f + 0.05f))
        )
        drawCircle(
          color = Color(0xFF15803D),
          radius = treeRadius,
          center = Offset(width * 0.9f, height * (i * 0.18f + 0.05f))
        )
      }

      // Tall grass patch (Wild encounter zone)
      drawRoundRect(
        color = Color(0xFF16A34A),
        topLeft = Offset(width * 0.18f, height * 0.25f),
        size = Size(width * 0.18f, height * 0.45f),
        cornerRadius = CornerRadius(6f, 6f)
      )

      // NPC sprite (Prof. Oak / Birch)
      drawCircle(
        color = Color(0xFF8B5CF6),
        radius = width * 0.045f,
        center = Offset(width * 0.74f, height * 0.44f)
      )

      // Player Sprite (Dynamic based on coordinates)
      val pNormX = (gameState.playerX / 40.0f).coerceIn(0.18f, 0.82f)
      val pNormY = (gameState.playerY / 40.0f).coerceIn(0.18f, 0.72f)
      val pxCenter = Offset(width * pNormX, height * pNormY)

      drawCircle(
        color = Color(0xFF1D4ED8),
        radius = width * 0.055f,
        center = pxCenter
      )
      drawCircle(
        color = Color(0xFFEF4444),
        radius = width * 0.035f,
        center = Offset(pxCenter.x, pxCenter.y - width * 0.03f)
      )
    }

    // -------------------------------------------------------------
    // CLASSIC POKÉMON DIALOGUE BALLOON (MATCHING SCREENSHOT)
    // -------------------------------------------------------------
    val bubbleLeft = width * 0.04f
    val bubbleTop = height * 0.62f
    val bubbleWidth = width * 0.92f
    val bubbleHeight = height * 0.32f

    // Drop shadow
    drawRoundRect(
      color = Color(0x55000000),
      topLeft = Offset(bubbleLeft + 2f, bubbleTop + 2f),
      size = Size(bubbleWidth, bubbleHeight),
      cornerRadius = CornerRadius(14f, 14f)
    )

    // Main white dialogue body
    drawRoundRect(
      color = Color(0xFFFFFFFF),
      topLeft = Offset(bubbleLeft, bubbleTop),
      size = Size(bubbleWidth, bubbleHeight),
      cornerRadius = CornerRadius(14f, 14f)
    )

    // Double border: outer subtle blue-grey, inner fine line
    drawRoundRect(
      color = Color(0xFF6B7280),
      topLeft = Offset(bubbleLeft, bubbleTop),
      size = Size(bubbleWidth, bubbleHeight),
      cornerRadius = CornerRadius(14f, 14f),
      style = Stroke(width = 2.5f)
    )
    drawRoundRect(
      color = Color(0xFF9CA3AF),
      topLeft = Offset(bubbleLeft + 2.5f, bubbleTop + 2.5f),
      size = Size(bubbleWidth - 5f, bubbleHeight - 5f),
      cornerRadius = CornerRadius(11f, 11f),
      style = Stroke(width = 1f)
    )
  }

  // Dialogue Text & Animated Blinking Prompt Cursor (Matching "Now tell me. Are you a boy? 0")
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(start = 22.dp, end = 22.dp, bottom = 10.dp),
    contentAlignment = Alignment.BottomStart
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp)
    ) {
      Text(
        text = gameState.dialogText,
        color = Color(0xFF111827),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        lineHeight = 15.sp,
        maxLines = 2
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp)
      ) {
        Text(
          text = if (isIntroScreen) "0" else "▶",
          color = Color(0xFF374151),
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        // Blinking indicator arrow ▼
        if (blinkAnim > 0.5f) {
          Text(
            text = "▼",
            color = Color(0xFFEF4444),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
          )
        }
      }
    }
  }
}

/**
 * Authentic Retro LCD pixel scanlines and grid mesh overlay
 */
@Composable
private fun LcdPixelMeshOverlay() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val height = size.height
    val width = size.width

    // Fine horizontal scanline mesh
    var y = 0f
    val scanStep = 3.5f
    while (y < height) {
      drawLine(
        color = Color(0x12000000),
        start = Offset(0f, y),
        end = Offset(width, y),
        strokeWidth = 0.8f
      )
      y += scanStep
    }

    // Fine vertical LCD column mesh
    var x = 0f
    val colStep = 3.5f
    while (x < width) {
      drawLine(
        color = Color(0x0A000000),
        start = Offset(x, 0f),
        end = Offset(x, height),
        strokeWidth = 0.6f
      )
      x += colStep
    }
  }
}

/**
 * Computer Vision Detection overlay (color-coded bounding boxes)
 */
@Composable
private fun VisionOverlayCanvas(gameState: GameStateSnapshot) {
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val boxWidth = maxWidth
    val boxHeight = maxHeight

    for (det in gameState.detections) {
      val (borderColor, labelColor) = when (det.category) {
        VisionCategory.PLAYER -> NeonCyan to Color(0xFF0891B2)
        VisionCategory.ENEMY -> RubyAction to Color(0xFFDC2626)
        VisionCategory.HP_BAR -> EmeraldRam to Color(0xFF059669)
        VisionCategory.DIALOG -> Color(0xFFF59E0B) to Color(0xFFD97706)
        VisionCategory.WAYPOINT -> Color(0xFFA855F7) to Color(0xFF9333EA)
        else -> Color.White to Color.Gray
      }

      val leftDp = boxWidth * det.xNorm
      val topDp = boxHeight * det.yNorm
      val widthDp = boxWidth * det.widthNorm
      val heightDp = boxHeight * det.heightNorm

      Box(
        modifier = Modifier
          .size(widthDp, heightDp)
          .padding(start = leftDp, top = topDp)
          .border(1.dp, borderColor, RoundedCornerShape(3.dp))
      ) {
        // Tag badge
        Box(
          modifier = Modifier
            .background(labelColor, RoundedCornerShape(bottomEnd = 3.dp))
            .padding(horizontal = 3.dp, vertical = 1.dp)
        ) {
          Text(
            text = "${det.label} ${(det.confidence * 100).toInt()}%",
            color = Color.White,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
