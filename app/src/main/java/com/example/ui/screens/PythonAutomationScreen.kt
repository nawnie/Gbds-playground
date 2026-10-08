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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import com.example.model.DatasetSampleType
import com.example.ui.MainViewModel
import com.example.ui.theme.CobaltCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import java.io.File

@Composable
fun PythonAutomationScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val scripts by viewModel.pythonEngine.scripts.collectAsState()
  val executionStatus by viewModel.pythonEngine.executionStatus.collectAsState()
  val terminalOutput by viewModel.pythonEngine.terminalOutput.collectAsState()
  val telemetry by viewModel.aiHarness.telemetry.collectAsState()
  val dataset by viewModel.aiHarness.dataset.collectAsState()

  var selectedScriptId by remember { mutableStateOf(scripts.firstOrNull()?.id ?: "") }
  val activeScript = scripts.firstOrNull { it.id == selectedScriptId } ?: scripts.firstOrNull()

  var isEditingCode by remember { mutableStateOf(false) }
  var editableCode by remember(activeScript) { mutableStateOf(activeScript?.code ?: "") }

  var filterType by remember { mutableStateOf<DatasetSampleType?>(null) }
  var showExportDialog by remember { mutableStateOf(false) }
  var exportedJsonl by remember { mutableStateOf("") }

  val filteredDataset = if (filterType == null) dataset else dataset.filter { it.sampleType == filterType }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    item {
      // Header
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Code, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "PYTHON AUTOMATION & DATASET MAKER",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
          )
          Text(
            text = "Gameplay Training Records • Fights • Nav • Screenshots",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // Performance Telemetry Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CobaltCard)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          TelemetryStat("FPS", String.format("%.1f", telemetry.fps), EmeraldRam)
          TelemetryStat("LOOP LATENCY", "${telemetry.loopLatencyMs}ms", NeonCyan)
          TelemetryStat("DECISIONS", "${telemetry.decisionsPerMin}/m", Color(0xFF38BDF8))
          TelemetryStat("DATA SAMPLES", "${dataset.size}", Color(0xFFA855F7))
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // Verified Python Interpreter Lifecycle Status Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070E20)),
        shape = RoundedCornerShape(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(executionStatus.state.badgeColorHex))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = executionStatus.state.label,
              color = Color.White,
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = executionStatus.message,
              color = TextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            executionStatus.detail?.let {
              Text(
                text = it,
                color = TextSecondary,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }

    // Python Scripts Section
    item {
      Text(
        text = "AUTOMATION SCRIPT SANDBOX (PYTHON 3.11):",
        color = NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        scripts.forEach { script ->
          val isSelected = script.id == selectedScriptId
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFF0F172A))
              .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) NeonCyan else Color(0xFF334155),
                shape = RoundedCornerShape(8.dp)
              )
              .clickable {
                selectedScriptId = script.id
                editableCode = script.code
              }
              .padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = script.filename,
              color = if (isSelected) NeonCyan else TextSecondary,
              fontSize = 9.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              fontFamily = FontFamily.Monospace,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }

    // Active Script Details & Code Editor
    if (activeScript != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
          shape = RoundedCornerShape(10.dp)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = activeScript.name,
                  color = TextPrimary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = activeScript.description,
                  color = TextSecondary,
                  fontSize = 10.sp
                )
              }

              // Run Script Button
              Button(
                onClick = {
                  if (isEditingCode) {
                    viewModel.pythonEngine.updateScriptCode(activeScript.id, editableCode)
                    isEditingCode = false
                  }
                  viewModel.pythonEngine.executeScript(activeScript.id)
                },
                enabled = !activeScript.isExecuting,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldRam),
                modifier = Modifier.testTag("btn_run_python_script")
              ) {
                if (activeScript.isExecuting) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Run", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code editor area
            OutlinedTextField(
              value = editableCode,
              onValueChange = {
                editableCode = it
                isEditingCode = true
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
              textStyle = androidx.compose.ui.text.TextStyle(
                color = Color(0xFFE2E8F0),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
              ),
              colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF050811),
                unfocusedContainerColor = Color(0xFF050811),
                focusedIndicatorColor = NeonCyan,
                unfocusedIndicatorColor = Color(0xFF1E293B)
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
      }
    }

    // Terminal Output Box
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Terminal, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "TERMINAL STDOUT",
            color = NeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Text(
          text = "Clear Output",
          color = TextMuted,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier
            .clickable { viewModel.pythonEngine.clearTerminal() }
            .padding(4.dp)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(100.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF030712))
          .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Text(
          text = terminalOutput,
          color = EmeraldRam,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          lineHeight = 13.sp
        )
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // Massive Dataset Gameplay Training Records Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "GAMEPLAY TRAINING DATASET (${dataset.size} SAMPLES)",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Logged fight turns, navigation paths & screenshot frames",
            color = TextMuted,
            fontSize = 10.sp
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Export Button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFF1E3A8A))
              .clickable {
                exportedJsonl = viewModel.aiHarness.exportDatasetJsonl()
                showExportDialog = true
              }
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("btn_export_dataset")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Download, contentDescription = "Export", tint = NeonCyan, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Export JSONL", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
          }

          // Clear Button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFF334155))
              .clickable { viewModel.aiHarness.clearDataset() }
              .padding(horizontal = 6.dp, vertical = 4.dp)
              .testTag("btn_clear_dataset")
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(12.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Filter Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        DatasetFilterChip("ALL (${dataset.size})", isSelected = filterType == null, onClick = { filterType = null })
        DatasetFilterChip("FIGHTS (${dataset.count { it.sampleType == DatasetSampleType.FIGHT }})", isSelected = filterType == DatasetSampleType.FIGHT, onClick = { filterType = DatasetSampleType.FIGHT })
        DatasetFilterChip("NAV (${dataset.count { it.sampleType == DatasetSampleType.NAVIGATION }})", isSelected = filterType == DatasetSampleType.NAVIGATION, onClick = { filterType = DatasetSampleType.NAVIGATION })
        DatasetFilterChip("SNAPS (${dataset.count { it.sampleType == DatasetSampleType.SCREENSHOT }})", isSelected = filterType == DatasetSampleType.SCREENSHOT, onClick = { filterType = DatasetSampleType.SCREENSHOT })
      }

      Spacer(modifier = Modifier.height(8.dp))
    }

    // Dataset Sample Cards Stream
    if (filteredDataset.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No samples logged yet. Play the game or press buttons to generate training records!",
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    } else {
      items(filteredDataset.take(30)) { sample ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
          colors = CardDefaults.cardColors(containerColor = CobaltCard),
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Type badge
            val badgeBg = Color(sample.sampleType.badgeColorHex)
            Box(
              modifier = Modifier
                .background(badgeBg, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = sample.sampleType.name,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "${sample.scenario} • Frame ${sample.frameNumber}",
                  color = TextPrimary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Input: ${sample.inputCommand}",
                  color = NeonCyan,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }

              Text(
                text = "Zone: ${sample.zoneName} (${sample.playerCoords.first}, ${sample.playerCoords.second}) ${if (sample.inBattle) "• Battle vs ${sample.opponentName}" else ""}",
                color = TextSecondary,
                fontSize = 9.sp
              )

              if (sample.screenshotTag != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(10.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = sample.screenshotTag,
                    color = Color(0xFFC084FC),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Export JSONL Dialog
  if (showExportDialog) {
    var savedFile by remember { mutableStateOf<File?>(null) }
    var fileExportMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Export Dataset to Storage", color = TextPrimary) },
      text = {
        Column {
          Text(
            text = "Total ${dataset.size} samples formatted into JSONL for PaliGemma / Gemma / custom LoRA fine-tuning:",
            color = TextMuted,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .background(Color(0xFF050811), RoundedCornerShape(6.dp))
              .padding(8.dp)
          ) {
            Text(
              text = exportedJsonl.ifBlank { "Dataset is empty. Play or move to log samples." },
              color = EmeraldRam,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              lineHeight = 12.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Save File Button
          Button(
            onClick = {
              val f = viewModel.exportDatasetFile(context)
              savedFile = f
              fileExportMessage = "Saved: ${f.name} (${f.length()} bytes)\nPath: ${f.absolutePath}"
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
            modifier = Modifier.fillMaxWidth().testTag("btn_save_dataset_file")
          ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save File to App Storage", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          fileExportMessage?.let { msg ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = msg,
              color = EmeraldRam,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      },
      confirmButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = {
              clipboardManager.setText(AnnotatedString(exportedJsonl))
              viewModel.aiHarness.recordThought("Copied ${dataset.size} JSONL records to clipboard.")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
          ) {
            Text("Copy JSONL", color = Color.White, fontSize = 11.sp)
          }

          Button(
            onClick = { showExportDialog = false },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
          ) {
            Text("Done", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }
    )
  }
}

@Composable
private fun DatasetFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFF0F172A))
      .border(0.5.dp, if (isSelected) NeonCyan else Color(0xFF334155), RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 3.dp)
  ) {
    Text(
      text = text,
      color = if (isSelected) NeonCyan else TextMuted,
      fontSize = 8.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun TelemetryStat(label: String, value: String, valueColor: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      color = valueColor,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    Text(
      text = label,
      color = TextMuted,
      fontSize = 8.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}
