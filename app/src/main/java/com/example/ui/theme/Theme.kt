package com.example.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
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
    primary = VelvetPrimary,
    secondary = VelvetSecondary,
    tertiary = VelvetTertiary,
    background = VelvetBackground,
    surface = VelvetSurface,
    onPrimary = VelvetBackground,
    onSecondary = VelvetOnSurface,
    onTertiary = VelvetBackground,
    onBackground = VelvetOnSurface,
    onSurface = VelvetOnSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RosePrimary,
    secondary = RoseSecondary,
    tertiary = RoseTertiary,
    background = RomanticBackground,
    surface = RomanticSurface,
    onPrimary = RomanticSurface,
    onSecondary = RosePrimary,
    onTertiary = RomanticSurface,
    onBackground = Color(0xFF331E21),
    onSurface = Color(0xFF331E21)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For branded romantic aesthetic, we default dynamicColor to false
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
