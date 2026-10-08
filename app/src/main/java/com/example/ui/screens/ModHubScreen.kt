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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameModEntry
import com.example.model.GameplayRuleModifier
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ModHubScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val mods by viewModel.modEngine.mods.collectAsState()
  val gameplayRules by viewModel.modEngine.gameplayRules.collectAsState()
  val gameState by viewModel.consoleEngine.gameState.collectAsState()

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
          Icon(Icons.Default.Extension, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "ROM HACKS & MOD ENGINE HUB",
              color = TextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "IPS/BPS patches, gameplay rule injection & overhaul packs",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }

    // 2. IN-ENGINE GAMEPLAY RULE MODIFIERS (QUICK HOOKS)
    item {
      Text(
        text = "LIVE GAMEPLAY RULE MODIFIERS",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(6.dp))
    }

    items(gameplayRules) { rule ->
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 3.dp)
          .border(
            1.dp,
            if (rule.isEnabled) EmeraldRam else Color(0xFF1E293B),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFF334155))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = rule.category,
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = rule.name,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = rule.description,
              color = TextSecondary,
              fontSize = 11.sp
            )
          }

          Switch(
            checked = rule.isEnabled,
            onCheckedChange = { viewModel.toggleGameplayRule(rule.id) },
            colors = SwitchDefaults.colors(
              checkedThumbColor = EmeraldRam,
              checkedTrackColor = Color(0xFF065F46)
            )
          )
        }
      }
    }

    // 3. ROM HACKS & IPS PATCHES
    item {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "COMMUNITY ROM HACKS & IPS/BPS PATCHES",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(6.dp))
    }

    items(mods) { mod ->
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .border(
            1.dp,
            if (mod.isApplied) Color(0xFFA855F7) else Color(0xFF1E293B),
            RoundedCornerShape(12.dp)
          )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    .background(Color(mod.modType.badgeColorHex))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = mod.modType.displayName,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = mod.title,
                  color = TextPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Black
                )
              }
              Text(
                text = "${mod.version} by ${mod.author}",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }

            Button(
              onClick = { viewModel.toggleMod(mod.id) },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (mod.isApplied) Color(0xFFA855F7) else Color(0xFF1E293B)
              ),
              shape = RoundedCornerShape(8.dp)
            ) {
              if (mod.isApplied) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = if (mod.isApplied) "PATCHED" else "APPLY PATCH",
                color = if (mod.isApplied) Color.White else NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(text = mod.description, color = TextSecondary, fontSize = 11.sp)

          Spacer(modifier = Modifier.height(8.dp))

          // Feature list
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            mod.features.take(3).forEach { feat ->
              Text(
                text = "• $feat",
                color = TextMuted,
                fontSize = 10.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Technical Patch Telemetry
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFF0F172A))
              .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Checksum: ${mod.patchChecksum}",
              color = NeonCyan,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Size: ${mod.patchSizeKb} KB",
              color = TextMuted,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}
