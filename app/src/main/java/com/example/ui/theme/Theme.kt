package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkBackground,
    primaryContainer = CobaltPrimaryDark,
    onPrimaryContainer = ElectricBlue,
    secondary = ElectricBlue,
    onSecondary = DarkBackground,
    secondaryContainer = CobaltCard,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldRam,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurfaceElevated,
    onSurface = TextPrimary,
    surfaceVariant = CobaltCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
  )

private val LightColorScheme = DarkColorScheme // Handheld console UI is themed dark for immersive screen contrast

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
