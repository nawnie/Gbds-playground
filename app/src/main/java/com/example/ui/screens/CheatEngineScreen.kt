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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.model.CheatCodeEntry
import com.example.model.CheatEngineType
import com.example.model.GameScenario
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CheatEngineScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val cheats by viewModel.cheatEngine.cheats.collectAsState()
  val activeCodesCount by viewModel.cheatEngine.activeCodesCount.collectAsState()
  val agentConfig by viewModel.aiHarness.config.collectAsState()
  val gameState by viewModel.consoleEngine.gameState.collectAsState()

  var selectedFilter by remember { mutableStateOf<CheatEngineType?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }
  var testResultText by remember { mutableStateOf<String?>(null) }

  // Custom code form state
  var newTitle by remember { mutableStateOf("") }
  var newCode by remember { mutableStateOf("") }
  var newType by remember { mutableStateOf(CheatEngineType.GAMESHARK) }
  var newDesc by remember { mutableStateOf("") }

  val filteredCheats = cheats.filter {
    (selectedFilter == null || it.engineType == selectedFilter) &&
      (it.targetScenario == null || it.targetScenario == gameState.scenario)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    // 1. HEADER & ADD BUTTON
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Code, contentDescription = null, tint = EmeraldRam, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "GAMESHARK & ACTION REPLAY ENGINE",
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Active Codes: $activeCodesCount • Target: ${gameState.scenario.title}",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        Button(
          onClick = { showAddDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("add_cheat_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldRam, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Add Code", color = EmeraldRam, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }

    // 2. MEMORY CHEAT PERMISSION BANNER
    if (!agentConfig.memoryCheatsAllowed) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1E1E)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "MEMORY CHEATS DISABLED IN SETTINGS",
                color = Color(0xFFFCA5A5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "Enable 'Memory Cheats Allowed' in the AI Config tab to activate GameShark codes.",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }
    }

    // 3. ENGINE FILTER CHIPS
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterPill(
          label = "ALL ENGINES",
          isSelected = selectedFilter == null,
          onClick = { selectedFilter = null }
        )
        CheatEngineType.values().forEach { type ->
          FilterPill(
            label = type.displayName.take(12),
            isSelected = selectedFilter == type,
            onClick = { selectedFilter = type }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }

    // 4. CHEAT CODES LIST
    if (filteredCheats.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
              text = "No cheats found for ${gameState.scenario.title} under this filter. Tap 'Add Code' to add custom codes.",
              color = TextMuted,
              fontSize = 12.sp
            )
          }
        }
      }
    } else {
      items(filteredCheats) { cheat ->
        CheatCardItem(
          cheat = cheat,
          isCheatsAllowed = agentConfig.memoryCheatsAllowed,
          onToggle = { viewModel.toggleCheatCode(cheat.id) },
          onTest = {
            val res = viewModel.testCheat(cheat)
            testResultText = res.getOrNull() ?: res.exceptionOrNull()?.message
          }
        )
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }

  // TEST RESULT DIALOG
  if (testResultText != null) {
    AlertDialog(
      onDismissRequest = { testResultText = null },
      title = { Text("RAM Write Verification", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = { Text(testResultText ?: "", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
      confirmButton = {
        TextButton(onClick = { testResultText = null }) {
          Text("OK", color = NeonCyan)
        }
      },
      containerColor = DarkSurfaceElevated
    )
  }

  // ADD CODE DIALOG
  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Add Custom Cheat Code", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = newTitle,
            onValueChange = { newTitle = it },
            label = { Text("Cheat Title") },
            placeholder = { Text("e.g. Infinite Master Balls") },
            modifier = Modifier.fillMaxWidth()
          )

          // Engine Type Selector
          Text(text = "Engine Type:", color = TextSecondary, fontSize = 12.sp)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CheatEngineType.values().forEach { type ->
              val isSel = newType == type
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSel) NeonCyan else Color(0xFF1E293B))
                  .clickable { newType = type }
                  .padding(horizontal = 8.dp, vertical = 6.dp)
              ) {
                Text(
                  text = type.prefix,
                  color = if (isSel) Color.Black else TextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          OutlinedTextField(
            value = newCode,
            onValueChange = { newCode = it },
            label = { Text("Code Lines (Hex)") },
            placeholder = { Text("e.g. 01017CCF or 8202402C 0014") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newDesc,
            onValueChange = { newDesc = it },
            label = { Text("Description") },
            placeholder = { Text("What this code does...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.addCustomCheat(newTitle, newCode, newType, newDesc, gameState.scenario)
            showAddDialog = false
            newTitle = ""
            newCode = ""
            newDesc = ""
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldRam)
        ) {
          Text("Validate & Save", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = DarkSurfaceElevated
    )
  }
}

@Composable
private fun CheatCardItem(
  cheat: CheatCodeEntry,
  isCheatsAllowed: Boolean,
  onToggle: () -> Unit,
  onTest: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(
        1.dp,
        if (cheat.isEnabled) EmeraldRam else Color(0xFF1E293B),
        RoundedCornerShape(12.dp)
      )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                  when (cheat.engineType) {
                    CheatEngineType.GAMESHARK -> Color(0xFF2563EB)
                    CheatEngineType.CODEBREAKER -> Color(0xFF059669)
                    CheatEngineType.ACTION_REPLAY -> Color(0xFFD97706)
                  }
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = cheat.engineType.prefix,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = cheat.title,
              color = TextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(2.dp))
          Text(text = cheat.description, color = TextSecondary, fontSize = 11.sp)
        }

        Switch(
          checked = cheat.isEnabled,
          onCheckedChange = { onToggle() },
          colors = SwitchDefaults.colors(
            checkedThumbColor = EmeraldRam,
            checkedTrackColor = Color(0xFF065F46)
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Raw code and address verification
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF0A0F1D))
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Code: ${cheat.rawCode.replace("\n", " • ")}",
            color = NeonCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "RAM Target: ${cheat.targetAddressHex} = ${cheat.targetValueHex}",
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        OutlinedButton(
          onClick = { onTest() },
          shape = RoundedCornerShape(6.dp)
        ) {
          Icon(Icons.Default.FlashOn, contentDescription = null, tint = EmeraldRam, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Verify Poke", color = EmeraldRam, fontSize = 10.sp)
        }
      }
    }
  }
}

@Composable
private fun FilterPill(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) NeonCyan else Color(0xFF1E293B))
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      color = if (isSelected) Color.Black else TextPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}
