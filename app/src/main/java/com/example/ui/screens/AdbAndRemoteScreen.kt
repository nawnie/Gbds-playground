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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
fun AdbAndRemoteScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val adbStatus by viewModel.adbBridge.status.collectAsState()

  var ipInput by remember { mutableStateOf(adbStatus.ipAddress) }
  var portInput by remember { mutableStateOf(adbStatus.port.toString()) }
  var customCommand by remember { mutableStateOf("dumpsys meminfo") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    item {
      // Header
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Wifi, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "ADB OVER WI-FI & ENCRYPTED REMOTE",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
          )
          Text(
            text = "State Inspection • Shell Diagnostics • Cloud TLS Bridge",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 1. Wi-Fi ADB Connection Box
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CobaltCard),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .background(if (adbStatus.isConnected) EmeraldRam else RubyAction, CircleShape)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (adbStatus.isConnected) "ADB WI-FI CONNECTED" else "ADB DISCONNECTED",
                color = if (adbStatus.isConnected) EmeraldRam else RubyAction,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
              )
            }

            if (adbStatus.isConnected) {
              Text(
                text = "Ping: ${adbStatus.latencyMs}ms",
                color = NeonCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // IP and Port Inputs
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = ipInput,
              onValueChange = { ipInput = it },
              label = { Text("Device IP Address") },
              modifier = Modifier.weight(2f),
              singleLine = true,
              colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0A0F1D),
                unfocusedContainerColor = Color(0xFF0A0F1D),
                focusedIndicatorColor = NeonCyan,
                unfocusedIndicatorColor = Color(0xFF334155)
              )
            )

            OutlinedTextField(
              value = portInput,
              onValueChange = { portInput = it },
              label = { Text("Port") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0A0F1D),
                unfocusedContainerColor = Color(0xFF0A0F1D),
                focusedIndicatorColor = NeonCyan,
                unfocusedIndicatorColor = Color(0xFF334155)
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                val p = portInput.toIntOrNull() ?: 5555
                viewModel.adbBridge.connectToDevice(ipInput, p)
              },
              colors = ButtonDefaults.buttonColors(containerColor = if (adbStatus.isConnected) Color(0xFF1E3A8A) else EmeraldRam),
              modifier = Modifier.weight(1f).testTag("btn_adb_connect")
            ) {
              Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (adbStatus.isConnected) "Re-Sync Bridge" else "Connect ADB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (adbStatus.isConnected) {
              Button(
                onClick = { viewModel.adbBridge.disconnect() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier.weight(0.7f).testTag("btn_adb_disconnect")
              ) {
                Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Disconnect", fontSize = 11.sp)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 2. Encrypted Remote Control (Cloud Deployment) Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131F37)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (adbStatus.isRemoteEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = null,
                tint = if (adbStatus.isRemoteEncrypted) EmeraldRam else RubyAction,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "ENCRYPTED REMOTE CLOUD CONTROL",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }

            Switch(
              checked = adbStatus.isRemoteEncrypted,
              onCheckedChange = { viewModel.adbBridge.toggleRemoteEncryption() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmeraldRam
              )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Enables end-to-end encrypted remote control sessions via secure TLS cloud relays for headless deployment.",
            color = TextSecondary,
            fontSize = 10.sp
          )

          Spacer(modifier = Modifier.height(6.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF090D16), RoundedCornerShape(6.dp))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "SESSION TOKEN: ${adbStatus.remoteSessionToken}",
                color = NeonCyan,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "RELAY PROTOCOL: WSS / TLS 1.3 • AES-256-GCM Direct Channel",
                color = TextMuted,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 3. Quick ADB Shell Commands
    item {
      Text(
        text = "DIAGNOSTIC ADB SHELL COMMANDS:",
        color = NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        QuickAdbButton("screencap", onClick = { viewModel.adbBridge.executeAdbCommand("screencap") })
        QuickAdbButton("dumpsys", onClick = { viewModel.adbBridge.executeAdbCommand("dumpsys meminfo") })
        QuickAdbButton("proc/maps", onClick = { viewModel.adbBridge.executeAdbCommand("cat /proc/maps") })
        QuickAdbButton("keyevent A", onClick = { viewModel.adbBridge.executeAdbCommand("input keyevent 29") })
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Custom command executor
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = customCommand,
          onValueChange = { customCommand = it },
          label = { Text("adb shell <command>") },
          modifier = Modifier.weight(1f),
          singleLine = true,
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF0A0F1D),
            unfocusedContainerColor = Color(0xFF0A0F1D),
            focusedIndicatorColor = NeonCyan,
            unfocusedIndicatorColor = Color(0xFF334155)
          )
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
          onClick = {
            if (customCommand.isNotBlank()) {
              viewModel.adbBridge.executeAdbCommand(customCommand)
            }
          },
          modifier = Modifier
            .size(44.dp)
            .background(Color(0xFF1E3A8A), CircleShape)
            .testTag("btn_execute_adb_cmd")
        ) {
          Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Run", tint = NeonCyan, modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 4. Live Logcat Stream
    item {
      Text(
        text = "LIVE DIAGNOSTIC LOGCAT STREAM:",
        color = NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(6.dp))
    }

    items(adbStatus.recentLogs.reversed()) { logLine ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0xFF050811))
          .padding(horizontal = 6.dp, vertical = 4.dp)
      ) {
        Text(
          text = logLine,
          color = when {
            logLine.contains("ALERT") || logLine.contains("Disconnected") -> RubyAction
            logLine.contains("SUCCESS") || logLine.contains("connected") -> EmeraldRam
            logLine.contains("> adb") -> NeonCyan
            else -> Color(0xFFCBD5E1)
          },
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          lineHeight = 13.sp
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun QuickAdbButton(label: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFF1E293B))
      .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 5.dp)
  ) {
    Text(
      text = label,
      color = Color(0xFF38BDF8),
      fontSize = 9.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold
    )
  }
}
