package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.model.GameConsoleMode
import com.example.ui.MainViewModel
import com.example.ui.components.EdgePilotMenuOverlay
import com.example.ui.components.GbaRealisticConsole
import com.example.ui.components.Nds3dsRealisticConsole
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val gameState by viewModel.consoleEngine.gameState.collectAsState()
        val showVisionOverlay by viewModel.showVisionOverlay.collectAsState()
        val isMenuOpen by viewModel.isMenuOpen.collectAsState()

        // Handle Android Back button to close menu if open
        BackHandler(enabled = isMenuOpen) {
          viewModel.closeMenu()
        }

        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
          containerColor = Color.Black
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black)
          ) {
            // 1. PRIMARY UX: 1:1 REALISTIC CONSOLE HARDWARE CHASSIS
            // Dynamically adapts between GBA SP and Nintendo 3DS Dual-Screen layouts!
            // Operates with real input contract (press/release/held/simultaneous keys/touch)
            if (gameState.consoleMode == GameConsoleMode.NDS_3DS) {
              Nds3dsRealisticConsole(
                gameState = gameState,
                showVisionOverlay = showVisionOverlay,
                onKeyDown = { key -> viewModel.onKeyDown(key) },
                onKeyUp = { key -> viewModel.onKeyUp(key) },
                onCirclePad = { dx, dy -> viewModel.onCirclePad(dx, dy) },
                onTouchDown = { x, y -> viewModel.onTouchDown(x, y) },
                onTouchUp = { viewModel.onTouchUp() },
                onMenuPress = { viewModel.toggleMenu() },
                modifier = Modifier.fillMaxSize()
              )
            } else {
              GbaRealisticConsole(
                gameState = gameState,
                showVisionOverlay = showVisionOverlay,
                onKeyDown = { key -> viewModel.onKeyDown(key) },
                onKeyUp = { key -> viewModel.onKeyUp(key) },
                onMenuPress = { viewModel.toggleMenu() },
                modifier = Modifier.fillMaxSize()
              )
            }

            // 2. EDGEPILOT AI HARNESS MENU & DASHBOARD
            // Pauses the virtual console immediately while visible!
            EdgePilotMenuOverlay(
              viewModel = viewModel,
              isVisible = isMenuOpen,
              onDismiss = { viewModel.closeMenu() }
            )
          }
        }
      }
    }
  }
}
