package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ControlMode
import com.example.model.GameConsoleMode
import com.example.model.GameScenario
import com.example.ui.theme.CobaltCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ControlModeSwitcher(
  currentMode: ControlMode,
  onModeSelect: (ControlMode) -> Unit,
  currentConsole: GameConsoleMode,
  onConsoleToggle: () -> Unit,
  activeScenario: GameScenario,
  onScenarioSelect: (GameScenario) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(DarkSurfaceElevated)
      .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
      .padding(8.dp)
  ) {
    // Mode Switcher segmented buttons
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF0B132B), RoundedCornerShape(8.dp))
        .padding(3.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      ControlMode.entries.forEach { mode ->
        val isSelected = currentMode == mode
        val activeBg = when (mode) {
          ControlMode.HUMAN -> Color(0xFF2563EB)
          ControlMode.AI_CO_PILOT -> Color(0xFF0D9488)
          ControlMode.AI_AUTONOMOUS -> Color(0xFF7C3AED)
          ControlMode.AI_CHAOS_CHEAT -> RubyAction
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) activeBg else Color.Transparent)
            .clickable { onModeSelect(mode) }
            .padding(vertical = 6.dp)
            .testTag("mode_${mode.name.lowercase()}"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = mode.shortBadge,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Cartridge & Console Quick Info Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Console system toggle (GBA <-> 3DS)
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF1E293B))
          .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
          .clickable { onConsoleToggle() }
          .padding(horizontal = 6.dp, vertical = 3.dp)
          .testTag("toggle_console_mode"),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .background(EmeraldRam, CircleShape)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (currentConsole == GameConsoleMode.GBA) "GBA SP" else "3DS DUAL",
            color = NeonCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Cartridge quick switch info
      Text(
        text = "Cart: ${activeScenario.title.take(24)}",
        color = TextPrimary,
        fontSize = 10.sp,
        fontFamily = FontFamily.SansSerif,
        maxLines = 1,
        modifier = Modifier.weight(1f)
      )

      // Sync Latency Badge
      Box(
        modifier = Modifier
          .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
          .padding(horizontal = 4.dp, vertical = 2.dp)
      ) {
        Text(
          text = "⚡ DMA 16ms",
          color = EmeraldRam,
          fontSize = 8.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
