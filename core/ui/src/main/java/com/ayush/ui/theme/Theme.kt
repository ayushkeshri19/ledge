package com.ayush.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LedgeDarkColorScheme = darkColorScheme(
    primary = LedgeMd3Primary,
    onPrimary = LedgeMd3OnPrimary,
    primaryContainer = LedgeMd3PrimaryContainer,
    onPrimaryContainer = LedgeMd3OnPrimaryContainer,
    background = LedgeMd3Background,
    onBackground = LedgeMd3OnBackground,
    surface = LedgeMd3Surface,
    onSurface = LedgeMd3OnSurface,
    surfaceVariant = LedgeMd3SurfaceVariant,
    onSurfaceVariant = LedgeMd3OnSurfaceVariant,
    error = LedgeMd3Error,
    onError = LedgeMd3OnError,
    errorContainer = LedgeMd3ErrorContainer,
    onErrorContainer = LedgeMd3OnErrorContainer,
    outline = LedgeMd3Outline,
    outlineVariant = LedgeMd3OutlineVariant
)

private val LedgeLightColorScheme = lightColorScheme(
    primary = LedgeMd3PrimaryLight,
    onPrimary = LedgeMd3OnPrimaryLight,
    primaryContainer = LedgeMd3PrimaryContainerLight,
    onPrimaryContainer = LedgeMd3OnPrimaryContainerLight,
    background = LedgeMd3BackgroundLight,
    onBackground = LedgeMd3OnBackgroundLight,
    surface = LedgeMd3SurfaceLight,
    onSurface = LedgeMd3OnSurfaceLight,
    surfaceVariant = LedgeMd3SurfaceVariantLight,
    onSurfaceVariant = LedgeMd3OnSurfaceVariantLight,
    error = LedgeMd3ErrorLight,
    onError = LedgeMd3OnErrorLight,
    errorContainer = LedgeMd3ErrorContainerLight,
    onErrorContainer = LedgeMd3OnErrorContainerLight,
    outline = LedgeMd3OutlineLight,
    outlineVariant = LedgeMd3OutlineVariantLight
)

@Composable
fun LedgeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val ledgeColors = if (useDark) LedgeDarkColors else LedgeLightColors
    val colorScheme = if (useDark) LedgeDarkColorScheme else LedgeLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !useDark
            controller.isAppearanceLightNavigationBars = !useDark
        }
    }

    CompositionLocalProvider(LocalLedgeColors provides ledgeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LedgeTypography,
            shapes = LedgeShapes,
            content = content
        )
    }
}
