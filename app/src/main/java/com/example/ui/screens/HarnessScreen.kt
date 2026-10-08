package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameConsoleMode
import com.example.model.GameScenario
import com.example.ui.MainViewModel
import com.example.ui.components.ControlModeSwitcher
import com.example.ui.components.GbaSpChassis
import com.example.ui.components.ThoughtBubbleBar
import com.example.ui.components.VirtualScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun HarnessScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val gameState by viewModel.consoleEngine.gameState.collectAsState()
  val controlMode by viewModel.aiHarness.controlMode.collectAsState()
  val agentConfig by viewModel.aiHarness.config.collectAsState()
  val currentThought by viewModel.aiHarness.currentThought.collectAsState()
  val thoughtHistory by viewModel.aiHarness.thoughtHistory.collectAsState()
  val showVisionOverlay by viewModel.showVisionOverlay.collectAsState()
  val ttsMuted by viewModel.ttsAudioMuted.collectAsState()

  var showCartridgeDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .verticalScroll(scrollState)
      .padding(horizontal = 10.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Mode Switcher & Console Bar
    ControlModeSwitcher(
      currentMode = controlMode,
      onModeSelect = { viewModel.setControlMode(it) },
      currentConsole = gameState.consoleMode,
      onConsoleToggle = {
        val nextMode = if (gameState.consoleMode == GameConsoleMode.GBA) GameConsoleMode.NDS_3DS else GameConsoleMode.GBA
        viewModel.switchConsoleMode(nextMode)
      },
      activeScenario = gameState.scenario,
      onScenarioSelect = { viewModel.switchGameScenario(it) }
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Cartridge Quick Switch & Vision Overlay Chip
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF1E293B))
          .clickable { showCartridgeDialog = true }
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .testTag("btn_cartridge_selector")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Casino,
            contentDescription = "Swap Cartridge",
            tint = NeonCyan,
            modifier = Modifier.padding(end = 4.dp)
          )
          Text(
            text = "SWAP ROM: ${gameState.scenario.title.take(20)}...",
            color = TextPrimary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(if (showVisionOverlay) Color(0xFF065F46) else Color(0xFF334155))
          .clickable { viewModel.toggleVisionOverlay() }
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .testTag("btn_toggle_cv_overlay")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = "CV Overlay",
            tint = if (showVisionOverlay) EmeraldRam else TextMuted,
            modifier = Modifier.padding(end = 4.dp)
          )
          Text(
            text = if (showVisionOverlay) "CV OVERLAY: ON" else "CV OVERLAY: OFF",
            color = Color.White,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    val dataset by viewModel.aiHarness.dataset.collectAsState()
    val isRecordingEnabled by viewModel.aiHarness.isRecordingEnabled.collectAsState()

    // Massive Dataset Gameplay Recorder Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .background(Color(0xFF0F172A))
        .border(0.5.dp, if (isRecordingEnabled) Color(0xFFEF4444) else Color(0xFF334155), RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(if (isRecordingEnabled) Color(0xFFEF4444) else TextMuted, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isRecordingEnabled) "DATASET REC: ON (${dataset.size} SAMPLES)" else "DATASET REC: PAUSED",
          color = if (isRecordingEnabled) Color(0xFFFCA5A5) else TextMuted,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E3A8A))
            .clickable { viewModel.captureScreenshot() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("btn_capture_screenshot")
        ) {
          Text("📸 SNAP", color = NeonCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isRecordingEnabled) Color(0xFF7F1D1D) else Color(0xFF065F46))
            .clickable { viewModel.toggleGameplayRecording() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("btn_toggle_recording")
        ) {
          Text(if (isRecordingEnabled) "PAUSE" else "RECORD", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Virtual Console Game Screen (Screen + CV Overlay)
    VirtualScreen(
      gameState = gameState,
      showVisionOverlay = showVisionOverlay
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Live AI Spoken Thought Bubble
    ThoughtBubbleBar(
      currentThought = currentThought,
      personality = agentConfig.personality,
      isTtsMuted = ttsMuted,
      onToggleMute = { viewModel.toggleTtsMute() },
      onReplayThought = { thought ->
        viewModel.aiHarness.recordThought(thought)
      },
      thoughtHistory = thoughtHistory
    )

    Spacer(modifier = Modifier.height(6.dp))

    // GBA SP Clamshell Hardware Body with D-Pad & Action Buttons
    GbaSpChassis(
      consoleMode = gameState.consoleMode,
      onKeyPress = { key -> viewModel.sendGamepadInput(key) },
      onToggleVisionOverlay = { viewModel.toggleVisionOverlay() }
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Live Memory Quick Stats Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFF0F172A))
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "HP: ${gameState.playerHp}/${gameState.playerMaxHp}",
        color = if (gameState.playerHp <= 5) RubyAction else EmeraldRam,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "COINS: $${gameState.coins}",
        color = NeonCyan,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "CHEATS: ${gameState.activeCheatsCount} ACTIVE",
        color = if (gameState.activeCheatsCount > 0) RubyAction else TextMuted,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }

  // Cartridge Selection Dialog
  if (showCartridgeDialog) {
    AlertDialog(
      onDismissRequest = { showCartridgeDialog = false },
      title = {
        Text("Select Virtual Console Game", color = TextPrimary, fontWeight = FontWeight.Bold)
      },
      text = {
        Column {
          GameScenario.entries.forEach { scenario ->
            val isSelected = scenario == gameState.scenario
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFF1E293B))
                .clickable {
                  viewModel.switchGameScenario(scenario)
                  showCartridgeDialog = false
                }
                .padding(8.dp)
            ) {
              Column {
                Text(
                  text = scenario.title,
                  color = if (isSelected) NeonCyan else TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                Text(
                  text = "System: ${scenario.platform.displayName} | Zone: ${scenario.defaultZone}",
                  color = TextMuted,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showCartridgeDialog = false }) {
          Text("Cancel", color = NeonCyan)
        }
      }
    )
  }
}
