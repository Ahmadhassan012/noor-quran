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

private val NoorLightColorScheme = lightColorScheme(
  primary = Accent,
  onPrimary = Color.White,
  secondary = TextSecondary,
  onSecondary = TextPrimary,
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
  darkTheme: Boolean = false, // We strictly enforce Noor's light manuscript theme
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve pure brand aesthetic
  content: @Composable () -> Unit,
) {
  // Always use our custom manuscript light theme for Noor
  val colorScheme = NoorLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
