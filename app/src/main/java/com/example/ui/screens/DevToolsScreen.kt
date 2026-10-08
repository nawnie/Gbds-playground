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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.DevLogCategory
import com.example.model.MemoryRegion
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class DevToolTab(val title: String) {
  HEX_EDITOR("RAM Hex"),
  DEBUGGER("CPU & Step"),
  PALETTES_OAM("VRAM & OBJ"),
  IO_REGISTERS("I/O Regs"),
  LOG_STREAM("Event Logs")
}

@Composable
fun DevToolsScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val devEngine = viewModel.devTools
  val gameState by viewModel.consoleEngine.gameState.collectAsState()
  val speedMultiplier by devEngine.speedMultiplier.collectAsState()
  val devLogs by devEngine.devLogs.collectAsState()

  var selectedTab by remember { mutableStateOf(DevToolTab.HEX_EDITOR) }
  var selectedRegion by remember { mutableStateOf(MemoryRegion.EWRAM) }
  var hexOffsetRows by remember { mutableIntStateOf(0) }
  var logCategoryFilter by remember { mutableStateOf<DevLogCategory?>(null) }

  // Byte edit dialog state
  var editTargetAddress by remember { mutableStateOf<String?>(null) }
  var editByteValueInput by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    // 1. HEADER
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.BugReport, contentDescription = null, tint = RubyAction, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "ENGINE DEVELOPER WORKSPACE",
              color = TextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Low-level RAM hex editor, step debugger, hardware I/O & VRAM",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
    }

    // 2. SUB-TOOL TABS
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(DevToolTab.values()) { tab ->
          val isSel = selectedTab == tab
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSel) NeonCyan else Color(0xFF1E293B))
              .clickable { selectedTab = tab }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = tab.title,
              color = if (isSel) Color.Black else TextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }

    // 3. TAB CONTENT
    when (selectedTab) {
      DevToolTab.HEX_EDITOR -> {
        item {
          // Region selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "REGION: ${selectedRegion.displayName}", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              OutlinedButton(
                onClick = { hexOffsetRows = (hexOffsetRows - 16).coerceAtLeast(0) },
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(text = "Prev Page", fontSize = 10.sp)
              }
              OutlinedButton(
                onClick = { hexOffsetRows += 16 },
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(text = "Next Page", fontSize = 10.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Region pills
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(MemoryRegion.values()) { reg ->
              val isRegSel = selectedRegion == reg
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isRegSel) Color(0xFF0284C7) else Color(0xFF0F172A))
                  .clickable { selectedRegion = reg; hexOffsetRows = 0 }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(text = reg.name, color = if (isRegSel) Color.White else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
        }

        // Hex rows
        val hexRows = devEngine.generateHexRows(selectedRegion, hexOffsetRows, rowCount = 14)
        items(hexRows) { row ->
          Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF080C16)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 1.dp)
              .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(4.dp))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Address
              Text(
                text = row.addressHex,
                color = NeonCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )

              // 8 bytes chunk 1
              Text(
                text = row.bytes.take(8).joinToString(" ") { String.format("%02X", it) },
                color = TextPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )

              // 8 bytes chunk 2
              Text(
                text = row.bytes.drop(8).joinToString(" ") { String.format("%02X", it) },
                color = Color(0xFF93C5FD),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )

              // ASCII
              Text(
                text = row.ascii,
                color = EmeraldRam,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable {
                  editTargetAddress = row.addressHex
                  editByteValueInput = String.format("%02X", row.bytes.firstOrNull() ?: 0)
                }
              )
            }
          }
        }
      }

      DevToolTab.DEBUGGER -> {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "EXECUTION & STEP CONTROLS",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.height(8.dp))

              // Step Buttons
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { devEngine.stepSingleFrame() },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.SkipNext, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "+1 Frame", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                  onClick = { devEngine.stepMultipleFrames(10) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.FastForward, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "+10 Frames", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                  onClick = { devEngine.rewindFrame() },
                  colors = ButtonDefaults.buttonColors(containerColor = RubyAction),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.Replay, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Rewind", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Speed Multiplier
              Text(
                text = "EMULATION SPEED (${speedMultiplier}x)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.height(6.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.5f, 1.0f, 2.0f, 4.0f, 8.0f).forEach { spd ->
                  val isSpd = speedMultiplier == spd
                  Button(
                    onClick = { devEngine.setSpeedMultiplier(spd) },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (isSpd) NeonCyan else Color(0xFF0F172A)
                    ),
                    modifier = Modifier.weight(1f)
                  ) {
                    Text(
                      text = "${spd}x",
                      color = if (isSpd) Color.Black else TextPrimary,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // CPU Telemetry
              Text(
                text = "HARDWARE TELEMETRY",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.height(6.dp))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF0A0F1D))
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(text = "Frame Number: ${gameState.frameNumber}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(text = "Scanline (VCOUNT): ${(gameState.frameNumber % 228).toInt()}/227", color = NeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(text = "Clock Rate: 16.78 MHz ARM7TDMI", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(text = "Target Framerate: 59.73 FPS (GBA Native)", color = EmeraldRam, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(text = "KEYINPUT Hex Bitmask: ${gameState.inputBufferHex}", color = Color(0xFFFACC15), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
              }
            }
          }
        }
      }

      DevToolTab.PALETTES_OAM -> {
        item {
          Text(
            text = "HARDWARE PALETTES (256 COLORS)",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(6.dp))

          // Color grid swatches
          val colors = devEngine.getPaletteColors().take(64)
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            colors.chunked(16).forEach { rowColors ->
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                rowColors.forEach { pCol ->
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .height(20.dp)
                      .clip(RoundedCornerShape(3.dp))
                      .background(Color(pCol.colorHex))
                      .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "OAM HARDWARE SPRITE TABLE",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(6.dp))
        }

        val sprites = devEngine.getOamSprites()
        items(sprites) { sp ->
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "Sprite #${sp.id}", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              Text(text = "Pos: (${sp.x}, ${sp.y})", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
              Text(text = "Tile: ${sp.tileIndex}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
              Text(text = sp.sizeStr, color = EmeraldRam, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
          }
        }
      }

      DevToolTab.IO_REGISTERS -> {
        val regs = devEngine.getIoRegisters()
        items(regs) { reg ->
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = reg.name, color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = reg.addressHex, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(text = reg.valueHex, color = Color(0xFFFACC15), fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(text = reg.description, color = TextSecondary, fontSize = 11.sp)

              Spacer(modifier = Modifier.height(6.dp))
              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                reg.bitfieldDetails.forEach { bit ->
                  Text(text = "• $bit", color = TextMuted, fontSize = 10.sp)
                }
              }
            }
          }
        }
      }

      DevToolTab.LOG_STREAM -> {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Category filter pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
              item {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (logCategoryFilter == null) NeonCyan else Color(0xFF1E293B))
                    .clickable { logCategoryFilter = null }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(text = "ALL", color = if (logCategoryFilter == null) Color.Black else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
              items(DevLogCategory.values().filter { it != DevLogCategory.ALL }) { cat ->
                val isSel = logCategoryFilter == cat
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSel) NeonCyan else Color(0xFF1E293B))
                    .clickable { logCategoryFilter = cat }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(text = cat.label, color = if (isSel) Color.Black else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
              onClick = { devEngine.clearLogs() },
              shape = RoundedCornerShape(6.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text(text = "Clear", color = TextMuted, fontSize = 10.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
        }

        val filteredLogs = devLogs.filter { logCategoryFilter == null || it.category == logCategoryFilter }
        items(filteredLogs) { log ->
          Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF080C16)),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp)
              .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(log.category.badgeColorHex))
                  .padding(horizontal = 5.dp, vertical = 2.dp)
              ) {
                Text(text = log.tag, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              }

              Spacer(modifier = Modifier.width(8.dp))

              Text(
                text = log.message,
                color = TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }
  }

  // BYTE EDIT DIALOG
  if (editTargetAddress != null) {
    AlertDialog(
      onDismissRequest = { editTargetAddress = null },
      title = { Text("Poke Memory Byte", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(text = "Address: $editTargetAddress", color = NeonCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = editByteValueInput,
            onValueChange = { editByteValueInput = it },
            label = { Text("Byte Hex (00 - FF)") },
            placeholder = { Text("e.g. 14") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val byteVal = editByteValueInput.toIntOrNull(16) ?: 0
            editTargetAddress?.let { devEngine.editByte(it, byteVal) }
            editTargetAddress = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldRam)
        ) {
          Text("Commit Write", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { editTargetAddress = null }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = DarkSurfaceElevated
    )
  }
}
