package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.model.AiGoal
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.ui.MainViewModel
import com.example.ui.theme.CobaltCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldRam
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RubyAction
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiChatAndGoalsScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val messages by viewModel.aiHarness.chatMessages.collectAsState()
  val goals by viewModel.aiHarness.goals.collectAsState()
  val agentConfig by viewModel.aiHarness.config.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var showNewGoalDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    // 1. Goal Hierarchy Header Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(12.dp)),
      colors = CardDefaults.cardColors(containerColor = CobaltCard)
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Flag, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "ACTIVE GOAL STRATEGY",
              color = NeonCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFF1E3A8A))
              .clickable { showNewGoalDialog = true }
              .padding(horizontal = 6.dp, vertical = 2.dp)
              .testTag("btn_assign_new_goal")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = NeonCyan, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text("Assign Goal", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        val primaryGoal = goals.firstOrNull()
        if (primaryGoal != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = primaryGoal.title,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = primaryGoal.description,
            color = TextSecondary,
            fontSize = 10.sp
          )
          Spacer(modifier = Modifier.height(6.dp))

          // Progress Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            LinearProgressIndicator(
              progress = { primaryGoal.progressPercent / 100f },
              modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = EmeraldRam,
              trackColor = Color(0xFF0F172A),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${primaryGoal.progressPercent}%",
              color = EmeraldRam,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          // Subtasks list preview
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            primaryGoal.subTasks.take(2).forEach { sub ->
              Box(
                modifier = Modifier
                  .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "• $sub",
                  color = TextMuted,
                  fontSize = 8.sp,
                  maxLines = 1,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. Quick Suggestion Chips
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      QuickPromptChip("Why that move?", onClick = { viewModel.aiHarness.sendUserChatMessage("Why did you make that move?") })
      QuickPromptChip("Defeat Next Gym", onClick = { viewModel.aiHarness.sendUserChatMessage("Goal: Defeat the next gym leader without fainting.") })
      QuickPromptChip("Poke $999,999", onClick = { viewModel.aiHarness.sendUserChatMessage("Can you cheat some money into our game memory?") })
      QuickPromptChip("Search Wiki", onClick = { viewModel.aiHarness.sendUserChatMessage("Search game wiki for boss weaknesses and route items.") })
      QuickPromptChip("Speedrun Route", onClick = { viewModel.aiHarness.sendUserChatMessage("Optimize pathing to reach the next town as fast as possible.") })
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 3. Conversation Messages Stream
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFF090D16))
        .padding(8.dp),
      reverseLayout = false
    ) {
      items(messages) { msg ->
        ChatMessageBubble(message = msg)
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 4. Input Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = { Text("Chat with AI, ask why, assign goals...", fontSize = 11.sp, color = TextMuted) },
        modifier = Modifier
          .weight(1f)
          .testTag("chat_input_field"),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = CobaltCard,
          unfocusedContainerColor = CobaltCard,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedIndicatorColor = NeonCyan,
          unfocusedIndicatorColor = Color(0xFF334155)
        ),
        shape = RoundedCornerShape(20.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.width(6.dp))

      IconButton(
        onClick = {
          if (inputText.isNotBlank()) {
            viewModel.aiHarness.sendUserChatMessage(inputText)
            inputText = ""
          }
        },
        modifier = Modifier
          .size(44.dp)
          .background(NeonCyan, CircleShape)
          .testTag("btn_send_chat")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send",
          tint = Color.Black,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }

  // Assign New Goal Dialog
  if (showNewGoalDialog) {
    var goalTitle by remember { mutableStateOf("") }
    var goalDesc by remember { mutableStateOf("") }
    var goalTasks by remember { mutableStateOf("Navigate area, Battle trainers, Claim reward") }

    AlertDialog(
      onDismissRequest = { showNewGoalDialog = false },
      title = { Text("Assign New Goal to Agent", color = TextPrimary) },
      text = {
        Column {
          OutlinedTextField(
            value = goalTitle,
            onValueChange = { goalTitle = it },
            label = { Text("Goal Title (e.g. Catch a Ralts on Route 102)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = goalDesc,
            onValueChange = { goalDesc = it },
            label = { Text("Strategy / Tactical Instructions") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = goalTasks,
            onValueChange = { goalTasks = it },
            label = { Text("Subtasks (comma separated)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (goalTitle.isNotBlank()) {
              val subList = goalTasks.split(",").map { it.trim() }.filter { it.isNotBlank() }
              viewModel.aiHarness.addNewGoal(goalTitle, goalDesc, subList)
            }
            showNewGoalDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
          Text("Assign Goal", color = Color.Black)
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewGoalDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
  val isUser = message.sender == MessageSender.USER
  val isSystem = message.sender == MessageSender.SYSTEM

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = when {
      isUser -> Alignment.End
      isSystem -> Alignment.CenterHorizontally
      else -> Alignment.Start
    }
  ) {
    if (isSystem) {
      Box(
        modifier = Modifier
          .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = message.text,
          color = Color(0xFF94A3B8),
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    } else {
      Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
      ) {
        if (!isUser) {
          Box(
            modifier = Modifier
              .size(26.dp)
              .background(Color(0xFF1E3A8A), CircleShape)
              .border(1.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text("AI", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }
          Spacer(modifier = Modifier.width(6.dp))
        }

        Box(
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(
              RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
              )
            )
            .background(if (isUser) Color(0xFF2563EB) else CobaltCard)
            .border(0.5.dp, if (isUser) Color(0xFF3B82F6) else Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = message.text,
              color = TextPrimary,
              fontSize = 12.sp,
              lineHeight = 16.sp
            )

            // Memory poke code snippet badge if attached
            if (message.memoryPokeSnippet != null) {
              Spacer(modifier = Modifier.height(4.dp))
              Box(
                modifier = Modifier
                  .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = ">>> ${message.memoryPokeSnippet}",
                  color = EmeraldRam,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            // Action tag / Timestamp
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (message.actionTag != null) {
                Text(
                  text = "[${message.actionTag}]",
                  color = if (isUser) Color(0xFFBFDBFE) else NeonCyan,
                  fontSize = 8.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
              }
              Text(
                text = message.timestamp,
                color = if (isUser) Color(0xFF93C5FD) else TextMuted,
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

@Composable
private fun QuickPromptChip(text: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF1E293B))
      .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = text,
      color = NeonCyan,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.SansSerif
    )
  }
}
