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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.model.AiPersonality
import com.example.model.KnowledgeLevel
import com.example.ui.MainViewModel
import com.example.ui.theme.CobaltCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AiConfigDashboardScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val config by viewModel.aiHarness.config.collectAsState()
  val edgeModels by viewModel.aiHarness.edgeModels.collectAsState()
  val loraAdapters by viewModel.aiHarness.loraAdapters.collectAsState()

  var showAddLoraDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    item {
      // Header
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = null,
          tint = NeonCyan,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "AI AGENT CONFIGURATION",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
          )
          Text(
            text = "Google AI Edge Gallery Harness & Personality Tuning",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 1. AI Personality Section
    item {
      SectionHeader(title = "1. AI PERSONALITY TRAITS")
      Text(
        text = "Governs playstyle, dialogue handling, risk appetite, and TTS spoken tone.",
        color = TextMuted,
        fontSize = 11.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      AiPersonality.entries.forEach { personality ->
        val isSelected = config.personality == personality
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
              width = if (isSelected) 1.5.dp else 0.5.dp,
              color = if (isSelected) NeonCyan else Color(0xFF334155),
              shape = RoundedCornerShape(10.dp)
            )
            .clickable { viewModel.updatePersonality(personality) }
            .testTag("personality_${personality.id}"),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF132347) else CobaltCard
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = personality.displayName,
                  color = if (isSelected) NeonCyan else TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                if (isSelected) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .background(EmeraldRam, CircleShape)
                      .size(8.dp)
                  )
                }
              }
              Text(
                text = personality.tagline,
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = personality.description,
                color = TextSecondary,
                fontSize = 10.sp
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Voice Tone: ${personality.speechTone}",
                color = TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 2. Knowledge Level Section
    item {
      SectionHeader(title = "2. GAME KNOWLEDGE & EXECUTION MASTERY")
      Spacer(modifier = Modifier.height(8.dp))

      KnowledgeLevel.entries.forEach { level ->
        val isSelected = config.knowledgeLevel == level
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
              width = if (isSelected) 1.5.dp else 0.5.dp,
              color = if (isSelected) EmeraldRam else Color(0xFF334155),
              shape = RoundedCornerShape(10.dp)
            )
            .clickable { viewModel.updateKnowledgeLevel(level) }
            .testTag("knowledge_${level.id}"),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0F291E) else CobaltCard
          )
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = level.title,
                color = if (isSelected) EmeraldRam else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = "Precision: ${level.subPixelAccuracy}",
                color = NeonCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = level.description,
              color = TextSecondary,
              fontSize = 10.sp
            )
            Text(
              text = "Memory Mapping: ${level.memoryFamiliarity}",
              color = TextMuted,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 3. Google AI Edge Gallery Models
    item {
      SectionHeader(title = "3. GOOGLE AI EDGE GALLERY MODELS")
      Text(
        text = "On-device quantized models for vision, state estimation, and action buffers.",
        color = TextMuted,
        fontSize = 11.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      edgeModels.forEach { model ->
        val isSelected = model.id == config.selectedModelId
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
              width = if (isSelected) 1.5.dp else 0.5.dp,
              color = if (isSelected) NeonCyan else Color(0xFF334155),
              shape = RoundedCornerShape(10.dp)
            )
            .clickable { viewModel.aiHarness.selectModel(model.id) }
            .testTag("model_${model.id}"),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF132347) else CobaltCard
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = model.name,
                  color = if (isSelected) NeonCyan else TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                if (model.isVisionSupported) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .background(Color(0xFF047857), RoundedCornerShape(4.dp))
                      .padding(horizontal = 4.dp, vertical = 1.dp)
                  ) {
                    Text("VISION", color = Color.White, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                  }
                }
              }
              Text(
                text = "${model.architecture} • ${model.sizeMb} MB • ~${model.latencyMs}ms Latency",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
            if (isSelected) {
              Box(
                modifier = Modifier
                  .background(EmeraldRam, CircleShape)
                  .size(20.dp),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Check, contentDescription = "Active", tint = Color.Black, modifier = Modifier.size(14.dp))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 4. Custom LoRA Adapters
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        SectionHeader(title = "4. CUSTOM LoRA ADAPTERS")
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E3A8A))
            .clickable { showAddLoraDialog = true }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("btn_import_lora")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = "Import", tint = NeonCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Import LoRA", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
      Spacer(modifier = Modifier.height(8.dp))

      loraAdapters.forEach { lora ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
              width = if (lora.isApplied) 1.dp else 0.5.dp,
              color = if (lora.isApplied) Color(0xFFA855F7) else Color(0xFF334155),
              shape = RoundedCornerShape(10.dp)
            ),
          colors = CardDefaults.cardColors(
            containerColor = if (lora.isApplied) Color(0xFF261438) else CobaltCard
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = lora.name,
                color = if (lora.isApplied) Color(0xFFE9D5FF) else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "Target: ${lora.targetScenario} (+${lora.accuracyBonusPercent}% heuristic accuracy)",
                color = Color(0xFFC084FC),
                fontSize = 10.sp
              )
              Text(
                text = lora.description,
                color = TextSecondary,
                fontSize = 9.sp
              )
            }
            Switch(
              checked = lora.isApplied,
              onCheckedChange = { viewModel.aiHarness.toggleLora(lora.id) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFA855F7)
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 5. Behavior Parameters & Feature Toggles
    item {
      SectionHeader(title = "5. BEHAVIOR PARAMETERS & PERMISSIONS")
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CobaltCard)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Toggle: Memory Cheats
          ToggleRow(
            title = "Live RAM Cheating & Memory Poking",
            subtitle = "Allows AI to directly modify RAM offsets for fun or infinite resources",
            checked = config.memoryCheatsAllowed,
            onCheckedChange = {
              viewModel.updateConfig(config.copy(memoryCheatsAllowed = it))
            },
            badge = "LIVE RAM"
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Toggle: Web Search
          ToggleRow(
            title = "Web Access (Guides & Damage Calcs)",
            subtitle = "Permits agent to query game wikis for battle weaknesses & routes",
            checked = config.webSearchEnabled,
            onCheckedChange = {
              viewModel.updateConfig(config.copy(webSearchEnabled = it))
            },
            badge = "WEB"
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Toggle: Python Automation
          ToggleRow(
            title = "Local Python Script Execution",
            subtitle = "Enables advanced scripting routines, TAS stepping & dataset exports",
            checked = config.pythonExecEnabled,
            onCheckedChange = {
              viewModel.updateConfig(config.copy(pythonExecEnabled = it))
            },
            badge = "PYTHON 3.11"
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Slider: Decision Exploration Rate
          Text(
            text = "Exploration Rate: ${(config.explorationRate * 100).toInt()}%",
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
          Slider(
            value = config.explorationRate,
            onValueChange = { viewModel.updateConfig(config.copy(explorationRate = it)) },
            valueRange = 0.05f..0.95f,
            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
          )

          // Slider: Decision Temperature
          Text(
            text = "Temperature / Creativity: ${String.format("%.2f", config.decisionTemperature)}",
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
          Slider(
            value = config.decisionTemperature,
            onValueChange = { viewModel.updateConfig(config.copy(decisionTemperature = it)) },
            valueRange = 0.1f..1.5f,
            colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
          )

          // Slider: Action Interval Delay
          Text(
            text = "Action Interval Delay: ${config.maxActionDelayMs}ms",
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
          Slider(
            value = config.maxActionDelayMs.toFloat(),
            onValueChange = { viewModel.updateConfig(config.copy(maxActionDelayMs = it.toInt())) },
            valueRange = 16f..200f,
            colors = SliderDefaults.colors(thumbColor = EmeraldRam, activeTrackColor = EmeraldRam)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 6. Text-to-Speech (TTS) Thought Narration Settings
    item {
      SectionHeader(title = "6. TEXT-TO-SPEECH (TTS) SPOKEN THOUGHTS")
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CobaltCard)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          ToggleRow(
            title = "Speak Thoughts Aloud (TTS Engine)",
            subtitle = "Narrates tactical decisions in real-time as the AI plays the game",
            checked = config.ttsEnabled,
            onCheckedChange = {
              viewModel.updateConfig(config.copy(ttsEnabled = it))
            },
            badge = "TTS VOICE"
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Pitch Slider
          Text(
            text = "Voice Pitch: ${String.format("%.2f", config.ttsPitch)}x",
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
          Slider(
            value = config.ttsPitch,
            onValueChange = { viewModel.updateConfig(config.copy(ttsPitch = it)) },
            valueRange = 0.6f..1.6f,
            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
          )

          // Rate Slider
          Text(
            text = "Speech Rate: ${String.format("%.2f", config.ttsSpeechRate)}x",
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
          Slider(
            value = config.ttsSpeechRate,
            onValueChange = { viewModel.updateConfig(config.copy(ttsSpeechRate = it)) },
            valueRange = 0.6f..1.6f,
            colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
          )

          Button(
            onClick = {
              viewModel.ttsManager.speak("EdgePilot TTS active. Prepared to execute frame-perfect decisions.")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
            modifier = Modifier.fillMaxWidth().testTag("btn_test_tts")
          ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonCyan)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sample Voice Output", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Add Custom LoRA Dialog
  if (showAddLoraDialog) {
    var loraName by remember { mutableStateOf("") }
    var loraGame by remember { mutableStateOf("Pokémon Emerald") }
    var loraDesc by remember { mutableStateOf("Fine-tuned adapter weights") }

    AlertDialog(
      onDismissRequest = { showAddLoraDialog = false },
      title = { Text("Import Custom LoRA Adapter", color = TextPrimary) },
      text = {
        Column {
          OutlinedTextField(
            value = loraName,
            onValueChange = { loraName = it },
            label = { Text("LoRA Name (e.g. emerald_glitchless_v1.safetensors)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = loraGame,
            onValueChange = { loraGame = it },
            label = { Text("Target Game / ROM") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = loraDesc,
            onValueChange = { loraDesc = it },
            label = { Text("Description & Policy Notes") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (loraName.isNotBlank()) {
              viewModel.aiHarness.addCustomLora(loraName, loraGame, loraDesc)
            }
            showAddLoraDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
          Text("Import", color = Color.Black)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddLoraDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }
}

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    color = NeonCyan,
    fontSize = 12.sp,
    fontWeight = FontWeight.Black,
    fontFamily = FontFamily.Monospace,
    letterSpacing = 1.sp
  )
}

@Composable
private fun ToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  badge: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = title,
          color = TextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text(badge, color = NeonCyan, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
      }
      Text(
        text = subtitle,
        color = TextSecondary,
        fontSize = 10.sp
      )
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = NeonCyan
      )
    )
  }
}
