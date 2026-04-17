package com.ayush.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class LedgeColors(
    val bgDeep: Color,
    val bgSurface: Color,
    val bgCard: Color,
    val bgCard2: Color,
    val bgSheet: Color,
    val borderSubtle: Color,
    val borderMid: Color,
    val borderFocus: Color,
    val gold: Color,
    val goldAccent: Color,
    val goldDim: Color,
    val goldGlow: Color,
    val semanticGreen: Color,
    val semanticRed: Color,
    val semanticBlue: Color,
    val semanticPurple: Color,
    val greenDim: Color,
    val redDim: Color,
    val blueDim: Color,
    val purpleDim: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val textMuted2: Color,
    val textMuted3: Color,
    val isLight: Boolean
)

val LedgeDarkColors = LedgeColors(
    bgDeep = BgDeep,
    bgSurface = BgSurface,
    bgCard = BgCard,
    bgCard2 = BgCard2,
    bgSheet = BgSheet,
    borderSubtle = BorderSubtle,
    borderMid = BorderMid,
    borderFocus = BorderFocus,
    gold = Gold,
    goldAccent = GoldLight,
    goldDim = GoldDim,
    goldGlow = GoldGlow,
    semanticGreen = SemanticGreen,
    semanticRed = SemanticRed,
    semanticBlue = SemanticBlue,
    semanticPurple = SemanticPurple,
    greenDim = GreenDim,
    redDim = RedDim,
    blueDim = BlueDim,
    purpleDim = PurpleDim,
    textPrimary = TextPrimary,
    textMuted = TextMuted,
    textMuted2 = TextMuted2,
    textMuted3 = TextMuted3,
    isLight = false
)

val LedgeLightColors = LedgeColors(
    bgDeep = BgDeepLight,
    bgSurface = BgSurfaceLight,
    bgCard = BgCardLight,
    bgCard2 = BgCard2Light,
    bgSheet = BgSheetLight,
    borderSubtle = BorderSubtleLight,
    borderMid = BorderMidLight,
    borderFocus = BorderFocusLight,
    gold = GoldBaseLight,
    goldAccent = GoldAccentLight,
    goldDim = GoldDimLight,
    goldGlow = GoldGlowLight,
    semanticGreen = SemanticGreenLight,
    semanticRed = SemanticRedLight,
    semanticBlue = SemanticBlueLight,
    semanticPurple = SemanticPurpleLight,
    greenDim = GreenDimLight,
    redDim = RedDimLight,
    blueDim = BlueDimLight,
    purpleDim = PurpleDimLight,
    textPrimary = TextPrimaryLight,
    textMuted = TextMutedLight,
    textMuted2 = TextMuted2Light,
    textMuted3 = TextMuted3Light,
    isLight = true
)

val LocalLedgeColors = staticCompositionLocalOf<LedgeColors> {
    error("LedgeColors not provided. Wrap your content in LedgeTheme { }.")
}

object LedgeTheme {
    val colors: LedgeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLedgeColors.current
}
