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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.GameCheat
import com.example.model.MemoryWatchEntry
import com.example.ui.MainViewModel
import com.example.ui.theme.CobaltCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MemoryInspectorScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val gameState by viewModel.consoleEngine.gameState.collectAsState()
  val cheats by viewModel.consoleEngine.cheats.collectAsState()
  val agentConfig by viewModel.aiHarness.config.collectAsState()

  var showPokeDialog by remember { mutableStateOf(false) }
  var pokeAddress by remember { mutableStateOf("0x02024090") }
  var pokeValue by remember { mutableStateOf("0x000F423F") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    item {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Memory, contentDescription = null, tint = EmeraldRam, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "LIVE RAM INSPECTOR & CHEAT ENGINE",
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.SansSerif
            )
            Text(
              text = "Direct WRAM / IRAM Memory Mapping & DMA Poke",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E3A8A))
            .clickable { showPokeDialog = true }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("btn_open_poke_dialog")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Edit, contentDescription = "Poke", tint = NeonCyan, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Poke RAM", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // AI Permission Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = if (agentConfig.memoryCheatsAllowed) Color(0xFF142E1F) else Color(0xFF2E1919)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.AutoFixHigh,
            contentDescription = null,
            tint = if (agentConfig.memoryCheatsAllowed) EmeraldRam else RubyAction,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (agentConfig.memoryCheatsAllowed) "AI Live Memory Poking: AUTHORIZED" else "AI Live Memory Poking: DISABLED",
              color = if (agentConfig.memoryCheatsAllowed) EmeraldRam else RubyAction,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = if (agentConfig.memoryCheatsAllowed)
                "The agent can autonomously read & write memory offsets to execute cheat strategies and goals."
              else
                "Agent is restricted to read-only memory inspection.",
              color = TextSecondary,
              fontSize = 9.sp
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // One-Tap Cheats Section
    item {
      Text(
        text = "ONE-TAP GAME MEMORY CHEATS",
        color = NeonCyan,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "Inject real-time cheats directly into emulated virtual console registers.",
        color = TextMuted,
        fontSize = 10.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
    }

    items(cheats) { cheat ->
      CheatCardItem(
        cheat = cheat,
        onToggle = {
          val res = viewModel.consoleEngine.toggleCheat(cheat.id, allowCheats = agentConfig.memoryCheatsAllowed)
          if (res.isFailure) {
            viewModel.aiHarness.recordThought("Cheat rejected: 'Memory Cheats Allowed' is disabled in AI Settings.")
          }
        }
      )
      Spacer(modifier = Modifier.height(6.dp))
    }

    // Memory Watch Table Section
    item {
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "ACTIVE RAM REGISTER WATCH LIST",
        color = NeonCyan,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "Live addresses updating at 60 FPS state sync.",
        color = TextMuted,
        fontSize = 10.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
    }

    items(gameState.memoryWatch) { entry ->
      MemoryWatchRow(
        entry = entry,
        onToggleFreeze = { viewModel.consoleEngine.toggleFreezeMemory(entry.addressHex) },
        onQuickPoke = {
          pokeAddress = entry.addressHex
          pokeValue = entry.valueHex
          showPokeDialog = true
        }
      )
      Spacer(modifier = Modifier.height(4.dp))
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Memory Poke Dialog
  if (showPokeDialog) {
    AlertDialog(
      onDismissRequest = { showPokeDialog = false },
      title = { Text("Direct RAM Poke (Live Write)", color = TextPrimary) },
      text = {
        Column {
          Text(
            text = "Inject a value directly into the virtual console's memory bus.",
            color = TextMuted,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = pokeAddress,
            onValueChange = { pokeAddress = it },
            label = { Text("Memory Address (Hex, e.g. 0x02024090)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = pokeValue,
            onValueChange = { pokeValue = it },
            label = { Text("Value to Write (Hex, e.g. 0x000F423F)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val res = viewModel.consoleEngine.pokeMemory(pokeAddress, pokeValue, allowCheats = agentConfig.memoryCheatsAllowed)
            if (res.isSuccess) {
              viewModel.aiHarness.recordThought("Manual RAM poke executed at $pokeAddress with value $pokeValue.")
            } else {
              viewModel.aiHarness.recordThought("Poke blocked: 'Memory Cheats Allowed' is disabled in AI Settings.")
            }
            showPokeDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldRam)
        ) {
          Text("Poke Address", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPokeDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }
}

@Composable
private fun CheatCardItem(
  cheat: GameCheat,
  onToggle: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = if (cheat.isEnabled) 1.dp else 0.5.dp,
        color = if (cheat.isEnabled) RubyAction else Color(0xFF334155),
        shape = RoundedCornerShape(8.dp)
      )
      .testTag("cheat_${cheat.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (cheat.isEnabled) Color(0xFF261017) else CobaltCard
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = cheat.title,
            color = if (cheat.isEnabled) RubyAction else TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .background(Color(0xFF0F172A), RoundedCornerShape(3.dp))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(cheat.category, color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
          }
        }
        Text(
          text = cheat.description,
          color = TextSecondary,
          fontSize = 10.sp
        )
        Text(
          text = "Hook: ${cheat.addressHex} -> ${cheat.activeValueHex}",
          color = TextMuted,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Switch(
        checked = cheat.isEnabled,
        onCheckedChange = { onToggle() },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = RubyAction
        )
      )
    }
  }
}

@Composable
private fun MemoryWatchRow(
  entry: MemoryWatchEntry,
  onToggleFreeze: () -> Unit,
  onQuickPoke: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFF0F172A))
      .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Address & Label
      Column(modifier = Modifier.weight(1.3f)) {
        Text(
          text = entry.addressHex,
          color = NeonCyan,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = entry.label,
          color = TextPrimary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        )
      }

      // Live Value (Hex & Dec)
      Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.End
      ) {
        Text(
          text = entry.valueHex,
          color = EmeraldRam,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "(${entry.decimalValue})",
          color = TextMuted,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Freeze Toggle Button
      IconButton(
        onClick = onToggleFreeze,
        modifier = Modifier.size(28.dp)
      ) {
        Icon(
          imageVector = if (entry.isFrozen) Icons.Default.Lock else Icons.Default.LockOpen,
          contentDescription = "Freeze Address",
          tint = if (entry.isFrozen) RubyAction else TextMuted,
          modifier = Modifier.size(16.dp)
        )
      }

      // Quick Edit / Poke Button
      IconButton(
        onClick = onQuickPoke,
        modifier = Modifier.size(28.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = "Poke Value",
          tint = NeonCyan,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
