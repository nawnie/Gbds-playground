package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiPersonality
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ThoughtBubbleBar(
  currentThought: String,
  personality: AiPersonality,
  isTtsMuted: Boolean,
  onToggleMute: () -> Unit,
  onReplayThought: (String) -> Unit,
  thoughtHistory: List<Pair<String, String>>,
  modifier: Modifier = Modifier
) {
  var showHistoryDialog by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
  val bar1Height by infiniteTransition.animateFloat(
    initialValue = 4f,
    targetValue = 16f,
    animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "bar1"
  )
  val bar2Height by infiniteTransition.animateFloat(
    initialValue = 14f,
    targetValue = 6f,
    animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "bar2"
  )
  val bar3Height by infiniteTransition.animateFloat(
    initialValue = 8f,
    targetValue = 18f,
    animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "bar3"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(
        Brush.linearGradient(
          colors = listOf(DarkSurfaceElevated, Color(0xFF0F172A))
        )
      )
      .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(12.dp))
      .padding(10.dp)
  ) {
    // Header Row with Persona badge and Audio Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Personality Badge
      Box(
        modifier = Modifier
          .background(Color(0xFF1E3A8A), RoundedCornerShape(6.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = "🧠 ${personality.displayName}",
          color = NeonCyan,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Animated Voice Waveform (shows TTS speech activity)
      if (!isTtsMuted) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          modifier = Modifier.height(18.dp)
        ) {
          Box(
            modifier = Modifier
              .width(3.dp)
              .height(bar1Height.dp)
              .background(NeonCyan, CircleShape)
          )
          Box(
            modifier = Modifier
              .width(3.dp)
              .height(bar2Height.dp)
              .background(ElectricBlue, CircleShape)
          )
          Box(
            modifier = Modifier
              .width(3.dp)
              .height(bar3Height.dp)
              .background(NeonCyan, CircleShape)
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // TTS Replay / Mute Button
      IconButton(
        onClick = onToggleMute,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = if (isTtsMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
          contentDescription = if (isTtsMuted) "Unmute AI Spoken Thoughts" else "Mute AI Spoken Thoughts",
          tint = if (isTtsMuted) TextMuted else NeonCyan,
          modifier = Modifier.size(18.dp)
        )
      }

      // History Button
      IconButton(
        onClick = { showHistoryDialog = !showHistoryDialog },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.History,
          contentDescription = "Thought History",
          tint = if (showHistoryDialog) NeonCyan else TextSecondary,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Spoken Thought text bubble
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFF0A0F1D))
        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
        .clickable { onReplayThought(currentThought) }
        .padding(8.dp)
    ) {
      Column {
        Text(
          text = "“$currentThought”",
          color = TextPrimary,
          fontSize = 12.sp,
          fontFamily = FontFamily.SansSerif,
          lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = if (isTtsMuted) "TTS Muted • Tap to replay" else "TTS Speaking live • Tap to replay aloud",
          color = TextMuted,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    // Expandable Thought History List
    AnimatedVisibility(visible = showHistoryDialog) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
          .height(140.dp)
          .background(Color(0xFF090D16), RoundedCornerShape(6.dp))
          .padding(6.dp)
      ) {
        Text(
          text = "RECENT AGENT COGNITIVE LOGS:",
          color = NeonCyan,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
          items(thoughtHistory) { (time, thought) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
            ) {
              Text(
                text = time,
                color = TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(55.dp)
              )
              Text(
                text = thought,
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif
              )
            }
          }
        }
      }
    }
  }
}
