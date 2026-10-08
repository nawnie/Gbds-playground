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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
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
import com.example.model.ButtonColorPreset
import com.example.model.HapticProfile
import com.example.model.ShellColorPreset
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ConsoleCustomizationScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val config by viewModel.customizationConfig.collectAsState()

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
          Icon(Icons.Default.Palette, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "CHASSIS & BUTTON CUSTOMIZATION",
              color = TextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Authentic shell finishes, key colors & tactile haptic tuning",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        OutlinedButton(
          onClick = {
            viewModel.setShellPreset(ShellColorPreset.COBALT_BLUE)
            viewModel.setButtonPreset(ButtonColorPreset.CLASSIC_CHARCOAL)
            viewModel.setHapticProfile(HapticProfile.MEDIUM)
          },
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Reset", color = NeonCyan, fontSize = 11.sp)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }

    // 2. LIVE CONSOLE PREVIEW CARD
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "LIVE CONSOLE PREVIEW",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Mini chassis preview
          Box(
            modifier = Modifier
              .size(width = 240.dp, height = 110.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    config.activeShellHighlightColor,
                    config.activeShellColor,
                    config.activeShellDeepColor
                  )
                )
              )
              .border(2.dp, config.activeShellHighlightColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
              .padding(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxSize(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Mini D-Pad
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(config.activeDpadColor)
                  .border(1.dp, Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(text = "✚", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
              }

              // Mini Screen
              Box(
                modifier = Modifier
                  .size(width = 90.dp, height = 70.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFF030712))
                  .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "EdgePilot",
                  color = NeonCyan,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace
                )
              }

              // Mini Action Buttons
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(config.activeButtonColor)
                    .border(1.dp, Color.Black.copy(alpha = 0.5f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = "B", color = config.activeButtonTextColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(config.activeButtonColor)
                    .border(1.dp, Color.Black.copy(alpha = 0.5f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = "A", color = config.activeButtonTextColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Active: ${config.shellPreset.displayName} Shell • ${config.buttonPreset.displayName} Keys",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 3. SHELL COLOR PRESETS
    item {
      Text(
        text = "SHELL COLOR EDITIONS",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ShellColorPreset.values().forEach { preset ->
          val isSelected = config.shellPreset == preset && !config.isCustomColorActive
          Card(
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) Color(0xFF1E293B) else DarkSurfaceElevated
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.setShellPreset(preset) }
              .border(
                1.dp,
                if (isSelected) NeonCyan else Color.Transparent,
                RoundedCornerShape(10.dp)
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
                // Color swatch
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                      Brush.verticalGradient(
                        colors = listOf(
                          Color(preset.shellHighlightHex),
                          Color(preset.shellBaseHex),
                          Color(preset.shellDeepHex)
                        )
                      )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = preset.displayName,
                    color = if (isSelected) NeonCyan else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = preset.description,
                    color = TextSecondary,
                    fontSize = 11.sp
                  )
                }
              }

              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 4. BUTTON COLOR PRESETS
    item {
      Text(
        text = "CONTROLLER BUTTON COLOR PRESETS",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ButtonColorPreset.values().forEach { preset ->
          val isSelected = config.buttonPreset == preset
          Card(
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) Color(0xFF1E293B) else DarkSurfaceElevated
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.setButtonPreset(preset) }
              .border(
                1.dp,
                if (isSelected) NeonCyan else Color.Transparent,
                RoundedCornerShape(10.dp)
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
                // Button swatch
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(preset.buttonFaceHex))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "A",
                    color = Color(preset.buttonTextHex),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = preset.displayName,
                    color = if (isSelected) NeonCyan else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = preset.description,
                    color = TextSecondary,
                    fontSize = 11.sp
                  )
                }
              }

              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 5. HAPTIC TOUCH ENGINE SETTINGS
    item {
      Text(
        text = "HAPTIC TOUCH & TACTILE FEEDBACK",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Vibration, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Haptic Vibration Intensity",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Button(
              onClick = { viewModel.hapticManager.triggerPress(config) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(text = "Test Pulse", color = NeonCyan, fontSize = 11.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Haptic Profile Selector Chips
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            HapticProfile.values().forEach { profile ->
              val isSelected = config.hapticProfile == profile
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A))
                  .clickable { viewModel.setHapticProfile(profile); viewModel.hapticManager.triggerPress(config.copy(hapticProfile = profile)) }
                  .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                  .padding(horizontal = 10.dp, vertical = 8.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(text = profile.displayName, color = if (isSelected) NeonCyan else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = profile.description, color = TextSecondary, fontSize = 10.sp)
                  }
                  if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Fine-Grained Toggles
          ToggleRow(
            label = "Vibrate on Button Press Down",
            description = "Simulates physical microswitch click travel",
            checked = config.hapticOnPress,
            onToggle = { viewModel.toggleHapticOnPress() }
          )
          Spacer(modifier = Modifier.height(8.dp))
          ToggleRow(
            label = "Vibrate on Button Release",
            description = "Subtle return spring tactile response",
            checked = config.hapticOnRelease,
            onToggle = { viewModel.toggleHapticOnRelease() }
          )
          Spacer(modifier = Modifier.height(8.dp))
          ToggleRow(
            label = "Vibrate on Touchscreen Tap",
            description = "Resistive digitizer stylus haptic feedback",
            checked = config.hapticOnTouchScreen,
            onToggle = { viewModel.toggleHapticOnTouch() }
          )
        }
      }
    }
  }
}

@Composable
private fun ToggleRow(
  label: String,
  description: String,
  checked: Boolean,
  onToggle: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
      Text(text = description, color = TextSecondary, fontSize = 10.sp)
    }
    Switch(
      checked = checked,
      onCheckedChange = { onToggle() },
      colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = Color(0xFF0E7490))
    )
  }
}
