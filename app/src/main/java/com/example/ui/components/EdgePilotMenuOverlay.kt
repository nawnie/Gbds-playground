package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.screens.AdbAndRemoteScreen
import com.example.ui.screens.AiChatAndGoalsScreen
import com.example.ui.screens.AiConfigDashboardScreen
import com.example.ui.screens.CheatEngineScreen
import com.example.ui.screens.ConsoleCustomizationScreen
import com.example.ui.screens.DevToolsScreen
import com.example.ui.screens.GrindBotScreen
import com.example.ui.screens.MemoryInspectorScreen
import com.example.ui.screens.ModHubScreen
import com.example.ui.screens.PythonAutomationScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EdgePilotMenuOverlay(
  viewModel: MainViewModel,
  isVisible: Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentTab by viewModel.currentTab.collectAsState()
  val controlMode by viewModel.aiHarness.controlMode.collectAsState()
  val gameState by viewModel.consoleEngine.gameState.collectAsState()
  val ttsMuted by viewModel.ttsAudioMuted.collectAsState()
  val showVisionOverlay by viewModel.showVisionOverlay.collectAsState()
  val currentThought by viewModel.aiHarness.currentThought.collectAsState()
  val thoughtHistory by viewModel.aiHarness.thoughtHistory.collectAsState()
  val agentConfig by viewModel.aiHarness.config.collectAsState()
  val dataset by viewModel.aiHarness.dataset.collectAsState()

  AnimatedVisibility(
    visible = isVisible,
    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    modifier = modifier.fillMaxSize()
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xE6050811))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss
        )
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 22.dp)
          .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
          .background(DarkBackground)
          .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {} // Consume click inside menu content
          )
      ) {
        // Top Header of Menu
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0F1D))
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.SportsEsports,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "EDGEPILOT AI HARNESS MENU",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
              )
              Text(
                text = "Cart: ${gameState.scenario.title.take(22)} • 60 FPS • DMA: 16ms",
                color = EmeraldRam,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          // Resume / Close Button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF1E3A8A))
              .clickable(onClick = onDismiss)
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("btn_resume_game"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = NeonCyan, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Resume", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // Quick Mode Switcher & Tools Ribbon
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          // Mode Switcher segmented buttons
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF090D16), RoundedCornerShape(8.dp))
              .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            ControlMode.entries.forEach { mode ->
              val isSelected = controlMode == mode
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
                  .clickable { viewModel.setControlMode(mode) }
                  .padding(vertical = 6.dp)
                  .testTag("menu_mode_${mode.name.lowercase()}"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = mode.shortBadge,
                  color = if (isSelected) TextPrimary else TextSecondary,
                  fontSize = 9.5.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Quick Action Chips (CV Overlay, TTS Voice, 📸 SNAP)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // CV Overlay Toggle Chip
            QuickActionPill(
              text = if (showVisionOverlay) "CV OVERLAY: ON" else "CV OVERLAY: OFF",
              color = if (showVisionOverlay) EmeraldRam else TextMuted,
              onClick = { viewModel.toggleVisionOverlay() }
            )

            // TTS Spoken Voice Toggle Chip
            QuickActionPill(
              text = if (ttsMuted) "TTS VOICE: MUTED" else "TTS VOICE: SPEAKING",
              color = if (ttsMuted) TextMuted else NeonCyan,
              onClick = { viewModel.toggleTtsMute() }
            )

            // Snap Screenshot Chip
            QuickActionPill(
              text = "📸 SNAP FRAME",
              color = Color(0xFFA855F7),
              onClick = { viewModel.captureScreenshot() }
            )

            // Dataset count badge
            Text(
              text = "${dataset.size} samples",
              color = TextMuted,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Navigation Tabs Row
        ScrollableTabRow(
          selectedTabIndex = currentTab.ordinal,
          containerColor = Color(0xFF0A0F1D),
          contentColor = NeonCyan,
          edgePadding = 8.dp,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
              color = NeonCyan,
              height = 2.5.dp
            )
          }
        ) {
          NavigationTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            Tab(
              selected = isSelected,
              onClick = { viewModel.setTab(tab) },
              text = {
                Text(
                  text = tab.title,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) NeonCyan else TextMuted,
                  fontFamily = FontFamily.SansSerif
                )
              },
              modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
          }
        }

        // Selected Tab Content Body
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(DarkBackground)
        ) {
          when (currentTab) {
            NavigationTab.HARNESS -> {
              // Quick Overview & Thought Bubble in Menu
              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(12.dp)
              ) {
                ThoughtBubbleBar(
                  currentThought = currentThought,
                  personality = agentConfig.personality,
                  isTtsMuted = ttsMuted,
                  onToggleMute = { viewModel.toggleTtsMute() },
                  onReplayThought = { thought -> viewModel.aiHarness.recordThought(thought) },
                  thoughtHistory = thoughtHistory
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                      text = "VIRTUAL CONSOLE HARDWARE CHASSIS",
                      color = NeonCyan,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      com.example.model.GameConsoleMode.entries.forEach { mode ->
                        val isCurrentMode = gameState.consoleMode == mode
                        Box(
                          modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrentMode) Color(0xFF1E3A8A) else Color(0xFF0F172A))
                            .border(
                              width = if (isCurrentMode) 1.5.dp else 0.5.dp,
                              color = if (isCurrentMode) NeonCyan else Color(0xFF334155),
                              shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.switchConsoleMode(mode) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                          contentAlignment = Alignment.Center
                        ) {
                          Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                              text = mode.displayName,
                              color = if (isCurrentMode) Color.White else TextMuted,
                              fontSize = 10.sp,
                              fontWeight = if (isCurrentMode) FontWeight.Bold else FontWeight.Normal,
                              fontFamily = FontFamily.SansSerif
                            )
                            Text(
                              text = mode.screenAspect,
                              color = if (isCurrentMode) EmeraldRam else TextMuted,
                              fontSize = 8.sp,
                              fontFamily = FontFamily.Monospace
                            )
                          }
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                      text = "QUICK ROM CARTRIDGE SWAP",
                      color = NeonCyan,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    com.example.model.GameScenario.entries.forEach { sc ->
                      val isCurrent = sc == gameState.scenario
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(vertical = 3.dp)
                          .clip(RoundedCornerShape(6.dp))
                          .background(if (isCurrent) Color(0xFF1E3A8A) else Color(0xFF0F172A))
                          .clickable { viewModel.switchGameScenario(sc) }
                          .padding(8.dp)
                      ) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Column {
                            Text(
                              text = sc.title,
                              color = if (isCurrent) NeonCyan else TextPrimary,
                              fontWeight = FontWeight.Bold,
                              fontSize = 11.sp
                            )
                            Text(
                              text = "${sc.platform.displayName} • ${sc.defaultZone}",
                              color = TextMuted,
                              fontSize = 9.sp,
                              fontFamily = FontFamily.Monospace
                            )
                          }
                          if (isCurrent) {
                            Text("LOADED", color = EmeraldRam, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            NavigationTab.GRIND_BOT -> GrindBotScreen(viewModel = viewModel)
            NavigationTab.CUSTOMIZE -> ConsoleCustomizationScreen(viewModel = viewModel)
            NavigationTab.CHEATS_CODES -> CheatEngineScreen(viewModel = viewModel)
            NavigationTab.MOD_HUB -> ModHubScreen(viewModel = viewModel)
            NavigationTab.DEV_TOOLS -> DevToolsScreen(viewModel = viewModel)
            NavigationTab.MEMORY_CHEATS -> MemoryInspectorScreen(viewModel = viewModel)
            NavigationTab.AI_CONFIG -> AiConfigDashboardScreen(viewModel = viewModel)
            NavigationTab.CHAT_GOALS -> AiChatAndGoalsScreen(viewModel = viewModel)
            NavigationTab.PYTHON_TASKS -> PythonAutomationScreen(viewModel = viewModel)
            NavigationTab.ADB_REMOTE -> AdbAndRemoteScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}

@Composable
private fun QuickActionPill(
  text: String,
  color: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFF0F172A))
      .border(0.5.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 7.dp, vertical = 4.dp)
  ) {
    Text(
      text = text,
      color = color,
      fontSize = 8.5.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}
