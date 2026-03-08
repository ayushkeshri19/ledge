package com.ayush.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ayush.ui.R


val DmSerifFontFamily = FontFamily(Font(R.font.dm_serif_regular))

val DmSansFontFamily = FontFamily(
    Font(R.font.dm_sans_light,      FontWeight.Light),
    Font(R.font.dm_sans_regular,    FontWeight.Normal),
    Font(R.font.dm_sans_medium,     FontWeight.Medium),
    Font(R.font.dm_sans_semibold,   FontWeight.SemiBold),
    Font(R.font.dm_sans_lightitalic,FontWeight.Light, FontStyle.Italic),
)

val DmMonoFontFamily = FontFamily(
    Font(R.font.dm_mono_light,   FontWeight.Light),
    Font(R.font.dm_mono_regular, FontWeight.Normal),
    Font(R.font.dm_mono_medium,  FontWeight.Medium),
)

object LedgeTextStyle {
    /** Hero balance figures — 42 sp, Syne ExtraBold, tracking -2 */
    val BalanceHero = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight  = FontWeight.ExtraBold,
        fontSize    = 42.sp,
        lineHeight  = 42.sp,
        letterSpacing = (-2).sp,
    )

    /** Section amounts / stats — 22 sp, Syne Bold */
    val AmountLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        letterSpacing = (-0.5).sp,
    )

    /** Card amounts / prices — 18 sp, Syne Bold */
    val AmountMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 18.sp,
        letterSpacing = (-0.5).sp,
    )

    /** Tabular transaction amounts — DM Mono Medium */
    val AmountMono = TextStyle(
        fontFamily  = DmMonoFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 15.sp,
    )

    /** Screen / section headers — Syne Bold 20 sp */
    val HeadingScreen = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 20.sp,
        letterSpacing = (-0.3).sp,
    )

    /** Card titles / section labels — Syne Bold 15 sp */
    val HeadingCard = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 15.sp,
        letterSpacing = (-0.2).sp,
    )

    /** ALL-CAPS micro labels — Syne SemiBold 11 sp, +3 tracking */
    val LabelCaps = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 11.sp,
        letterSpacing = 3.sp,
    )

    /** Body default — DM Sans Regular 14 sp */
    val Body = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
    )

    /** Body small — DM Sans Regular 12 sp */
    val BodySmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
    )

    /** Caption — DM Sans Light 10 sp, +0.8 tracking */
    val Caption = TextStyle(
        fontFamily    = DmSansFontFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 10.sp,
        letterSpacing = 0.8.sp,
    )

    /** Button label — DM Sans Medium 14 sp */
    val Button = TextStyle(
        fontFamily    = DmSansFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        letterSpacing = 0.3.sp,
    )
}

// ── Material 3 Typography mapping ───────────────
val LedgeTypography = Typography(
    displayLarge = LedgeTextStyle.BalanceHero,
    displayMedium = LedgeTextStyle.AmountLarge,
    displaySmall  = LedgeTextStyle.AmountMedium,

    headlineLarge  = LedgeTextStyle.HeadingScreen,
    headlineMedium = LedgeTextStyle.HeadingCard,
    headlineSmall  = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 13.sp,
    ),

    titleLarge  = LedgeTextStyle.HeadingCard,
    titleMedium = TextStyle(
        fontFamily    = DmSansFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall  = TextStyle(
        fontFamily    = DmSansFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        letterSpacing = 0.1.sp,
    ),

    bodyLarge  = LedgeTextStyle.Body,
    bodyMedium = LedgeTextStyle.BodySmall,
    bodySmall  = LedgeTextStyle.Caption,

    labelLarge  = LedgeTextStyle.Button,
    labelMedium = LedgeTextStyle.LabelCaps,
    labelSmall  = LedgeTextStyle.Caption,
)