package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
  primary = GoogleBlue40,
  onPrimary = Color.White,
  primaryContainer = GoogleBlue90,
  onPrimaryContainer = Color(0xFF001D36),

  secondary = GoogleGreen40,
  onSecondary = Color.White,
  secondaryContainer = GoogleGreen90,
  onSecondaryContainer = Color(0xFF00210A),

  tertiary = GoogleYellow40,
  onTertiary = Color.White,
  tertiaryContainer = GoogleYellow90,
  onTertiaryContainer = Color(0xFF2E1A00),

  error = GoogleRed40,
  onError = Color.White,
  errorContainer = GoogleRed90,
  onErrorContainer = Color(0xFF410002),

  background = SurfaceLight,
  onBackground = OnSurfaceLight,
  surface = SurfaceLight,
  onSurface = OnSurfaceLight,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = OnSurfaceVariantLight,
  outline = OutlineLight,
  outlineVariant = OutlineVariantLight,

  surfaceContainerLowest = SurfaceContainerLowestLight,
  surfaceContainerLow = SurfaceContainerLowLight,
  surfaceContainer = SurfaceContainerLight,
  surfaceContainerHigh = SurfaceContainerHighLight,
  surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val DarkColorScheme = darkColorScheme(
  primary = GoogleBlue80,
  onPrimary = Color(0xFF003062),
  primaryContainer = Color(0xFF00468B),
  onPrimaryContainer = GoogleBlue90,

  secondary = GoogleGreen80,
  onSecondary = Color(0xFF003915),
  secondaryContainer = Color(0xFF005322),
  onSecondaryContainer = GoogleGreen90,

  tertiary = GoogleYellow80,
  onTertiary = Color(0xFF452B00),
  tertiaryContainer = Color(0xFF633F00),
  onTertiaryContainer = GoogleYellow90,

  error = GoogleRed80,
  onError = Color(0xFF601410),
  errorContainer = Color(0xFF8C1D18),
  onErrorContainer = GoogleRed90,

  background = SurfaceDark,
  onBackground = OnSurfaceDark,
  surface = SurfaceDark,
  onSurface = OnSurfaceDark,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = OnSurfaceVariantDark,
  outline = OutlineDark,
  outlineVariant = OutlineVariantDark,

  surfaceContainerLowest = SurfaceContainerLowestDark,
  surfaceContainerLow = SurfaceContainerLowDark,
  surfaceContainer = SurfaceContainerDark,
  surfaceContainerHigh = SurfaceContainerHighDark,
  surfaceContainerHighest = SurfaceContainerHighestDark,
)

@Composable
fun GoogleFormTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme: ColorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = Shapes,
    content = content,
  )
}
