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

private val EmeraldDarkColorScheme =
  darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldOnContainerDark,
    secondary = FolderAmber,
    onSecondary = EmeraldOnPrimaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark
  )

private val AmoledDarkColorScheme =
  darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldOnContainerDark,
    secondary = FolderAmber,
    onSecondary = EmeraldOnPrimaryDark,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onBackground = AmoledOnSurface,
    onSurface = AmoledOnSurface,
    onSurfaceVariant = AmoledOnSurfaceVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = EmeraldOnContainerLight,
    secondary = FolderAmberDark,
    onSecondary = EmeraldOnPrimaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight
  )

@Composable
fun MyApplicationTheme(
  appPalette: AppColorPalette = AppColorPalette.EMERALD,
  themeMode: ThemeMode = ThemeMode.DARK,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val isDark = when (themeMode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
  }

  val colorScheme = when {
    appPalette == AppColorPalette.MATERIAL_YOU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    appPalette == AppColorPalette.AMOLED && isDark -> AmoledDarkColorScheme
    isDark -> EmeraldDarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
