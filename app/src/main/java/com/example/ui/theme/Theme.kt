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

import androidx.compose.ui.graphics.Color

private val NoorColorScheme = darkColorScheme(
  primary = Accent,
  onPrimary = BgBase,
  primaryContainer = AccentDim,
  onPrimaryContainer = TextPrimary,
  secondary = TextSecondary,
  onSecondary = BgBase,
  tertiary = TextArabic,
  background = BgBase,
  onBackground = TextPrimary,
  surface = BgSurface,
  onSurface = TextPrimary,
  surfaceVariant = BgElevated,
  onSurfaceVariant = TextSecondary,
  error = Error,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Premium dark theme by default
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = NoorColorScheme,
    typography = Typography,
    content = content
  )
}
