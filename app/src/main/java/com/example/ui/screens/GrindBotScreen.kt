package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GrindBotGoal
import com.example.model.GrindBotState
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
fun GrindBotScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val telemetry by viewModel.grindBot.telemetry.collectAsState()
  val gameState by viewModel.consoleEngine.gameState.collectAsState()

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
    label = "alpha"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    // 1. TOP HEADER & PRIMARY STATUS
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.SmartToy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "POKÉMON AI GRIND BOT",
              color = TextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Deterministic state machine • No LLM latency • Area patrol",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        // Live Bot State Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(telemetry.currentState.badgeColorHex).copy(alpha = if (telemetry.isEnabled) pulseAlpha else 0.8f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = telemetry.currentState.label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }

    // 2. PRIMARY MASTER CONTROLLER CARD
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(
            1.dp,
            if (telemetry.isEnabled) Color(0xFF10B981) else Color(0xFF1E293B),
            RoundedCornerShape(14.dp)
          )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Target Area: ${gameState.zoneName}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = telemetry.statusMessage,
                color = if (telemetry.currentState == GrindBotState.SHINY_LOCKED) Color(0xFFFACC15) else TextSecondary,
                fontSize = 11.sp,
                maxLines = 2
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              if (telemetry.isEnabled) {
                OutlinedButton(
                  onClick = { viewModel.closeMenu() },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("watch_console_button")
                ) {
                  Text("Watch on Console", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              Button(
                onClick = {
                  if (telemetry.isEnabled) viewModel.stopGrindBot() else viewModel.startGrindBot(telemetry.goal)
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (telemetry.isEnabled) RubyAction else Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("toggle_grind_bot_button")
              ) {
                Icon(
                  imageVector = if (telemetry.isEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (telemetry.isEnabled) "HALT BOT" else "START BOT",
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 4-METRIC GRID
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricPill(
              label = "ENCOUNTERS",
              value = "${telemetry.totalEncounters}",
              iconColor = NeonCyan,
              modifier = Modifier.weight(1f)
            )
            MetricPill(
              label = "SHINIES FOUND",
              value = "${telemetry.totalShiniesFound} ✨",
              iconColor = Color(0xFFFACC15),
              modifier = Modifier.weight(1f)
            )
            MetricPill(
              label = "BATTLES WON",
              value = "${telemetry.totalBattlesWon}",
              iconColor = EmeraldRam,
              modifier = Modifier.weight(1f)
            )
            MetricPill(
              label = "BALLS USED",
              value = "${telemetry.totalBallsThrown}",
              iconColor = Color(0xFFA855F7),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 3. SHINY HUNTING GOAL SELECTOR & TEST HOOK
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(
            1.dp,
            if (telemetry.goal == GrindBotGoal.SHINY_HUNT) Color(0xFFEAB308) else Color(0xFF1E293B),
            RoundedCornerShape(14.dp)
          )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "GRIND TILL SHINY IS FOUND",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
              )
            }

            if (telemetry.goal == GrindBotGoal.SHINY_HUNT) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFEAB308))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(text = "ACTIVE GOAL", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "The bot patrols tall grass indefinitely. When any wild Pokémon rolls shiny (sparkle PID), the bot immediately halts attacking, fires physical haptic alerts, announces the encounter, and preserves the Pokémon for capture.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Shiny protection toggle & Test hook
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Switch(
                checked = telemetry.pauseOnShiny,
                onCheckedChange = { viewModel.grindBot.updateSettings(pauseOnShiny = it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFACC15), checkedTrackColor = Color(0xFF854D0E))
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = "Pause & Lock on Shiny", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            OutlinedButton(
              onClick = {
                viewModel.forceShinyEncounter()
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("force_shiny_button")
            ) {
              Text(text = "Trigger Shiny ✨", color = Color(0xFFFACC15), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 4. GOAL SELECTION TABS
    item {
      Text(
        text = "SELECT BOT OBJECTIVE",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GrindBotGoal.values().forEach { goal ->
          val isSelected = telemetry.goal == goal
          Card(
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) Color(0xFF1E293B) else DarkSurfaceElevated
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.setGrindGoal(goal) }
              .border(
                1.dp,
                if (isSelected) NeonCyan else Color.Transparent,
                RoundedCornerShape(10.dp)
              )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = goal.displayName,
                  color = if (isSelected) NeonCyan else TextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = goal.description,
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              }

              if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 5. RECENT ENCOUNTERS FEED
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ENCOUNTER LOG STREAM (${telemetry.recentEncounters.size})",
          color = TextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        if (telemetry.recentEncounters.isNotEmpty()) {
          Text(
            text = "Latest: ${telemetry.lastEncounterName}",
            color = NeonCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
    }

    if (telemetry.recentEncounters.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
              text = "No encounters yet. Start the bot or walk in tall grass to log encounters.",
              color = TextMuted,
              fontSize = 12.sp
            )
          }
        }
      }
    } else {
      items(telemetry.recentEncounters) { enc ->
        Card(
          colors = CardDefaults.cardColors(
            containerColor = if (enc.isShiny) Color(0xFF282312) else DarkSurfaceElevated
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .border(
              1.dp,
              if (enc.isShiny) Color(0xFFEAB308) else Color(0xFF1E293B),
              RoundedCornerShape(8.dp)
            )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(if (enc.isShiny) Color(0xFFEAB308) else Color(0xFF334155)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "#${enc.encounterNumber}",
                  color = if (enc.isShiny) Color.Black else Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = enc.pokemonName,
                    color = if (enc.isShiny) Color(0xFFFACC15) else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                  if (enc.isShiny) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "✨ SHINY", color = Color(0xFFFACC15), fontSize = 10.sp, fontWeight = FontWeight.Black)
                  }
                }
                Text(
                  text = "Lv ${enc.level} • ${enc.actionTaken}",
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              }
            }

            Text(
              text = "Target Lock",
              color = if (enc.isShiny) Color(0xFFFACC15) else TextMuted,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MetricPill(
  label: String,
  value: String,
  iconColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF0F172A))
      .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Column {
      Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      Text(text = value, color = iconColor, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
    }
  }
}
