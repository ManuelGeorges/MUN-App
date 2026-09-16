package com.mianu.paymentapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MIANUCOM design tokens, ported from the web theme (see theme-details.md).
 *
 * Two layers, mirroring the CSS:
 *  - Primitive ramps ([MianuPrimary], [MianuGold], [MianuNeutral]) are decorative. Reach for these
 *    when building gradients, glows and ambient meshes.
 *  - Semantic tokens ([MianuColors]) are what components should actually consume. They resolve to a
 *    light or dark value via [LocalMianuColors].
 */

// region Primitive ramps

/** Blue ramp — gradients and decorative elements. */
object MianuPrimary {
    val P50 = Color(0xFFF0F6FB)
    val P100 = Color(0xFFDBE9F5)
    val P200 = Color(0xFFBBD5EB)
    val P300 = Color(0xFF8FB9DC)
    val P400 = Color(0xFF5A9BCB)
    val P500 = Color(0xFF3A80B5)
    val P600 = Color(0xFF21689B)
    val P700 = Color(0xFF1B547D)
    val P800 = Color(0xFF164562)
    val P900 = Color(0xFF0E3149)
    val P950 = Color(0xFF071D30)
}

/** Gold ramp — accents, awards, premium surfaces. */
object MianuGold {
    val G50 = Color(0xFFFBF7EC)
    val G100 = Color(0xFFF5EBD0)
    val G200 = Color(0xFFEBD6A1)
    val G300 = Color(0xFFDFBD6C)
    val G400 = Color(0xFFD4AF4E)
    val G500 = Color(0xFFC29A38)
    val G600 = Color(0xFF9E7B2C)
    val G700 = Color(0xFF7A5E23)
    val G800 = Color(0xFF57431B)
    val G900 = Color(0xFF3D2F14)
}

/** Grayscale ramp. */
object MianuNeutral {
    val N0 = Color(0xFFFFFFFF)
    val N50 = Color(0xFFFAFAF8)
    val N100 = Color(0xFFF4F4F1)
    val N200 = Color(0xFFE7E6E1)
    val N300 = Color(0xFFD3D2CB)
    val N400 = Color(0xFFA8A79E)
    val N500 = Color(0xFF878D96)
    val N600 = Color(0xFF5D5C55)
    val N700 = Color(0xFF44433D)
    val N800 = Color(0xFF2A2F37)
    val N900 = Color(0xFF23221E)
    val N950 = Color(0xFF171613)
}

// endregion

// region Semantic tokens

/**
 * The semantic palette. One instance is active at a time, exposed through [LocalMianuColors].
 *
 * Material's own [androidx.compose.material3.ColorScheme] can't express everything the web theme
 * uses (sunken/overlay/hover surfaces, four status colors, a gold accent), so this rides alongside
 * it. `MianuTheme` keeps the two in sync — Material components pick up the right colors, and
 * anything bespoke reads from here.
 */
data class MianuColors(
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceSunken: Color,
    val surfaceOverlay: Color,
    val surfaceHover: Color,
    val border: Color,
    val fg: Color,
    val fgMuted: Color,
    val accent: Color,
    val goldAccent: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val info: Color,
    val committeeAccent: Color = Color(0xFF5B96F7),
    val committeeBg: Color = Color(0xFF0F1E36),
    val committeeBorder: Color = Color(0xFF1E3A5F),
    val isDark: Boolean = true,
)

/**
 * High-contrast Black & White Theme mirroring the conference image:
 * Deep black backdrop (#000000 / #050608), rich carbon cards (#0F1218),
 * stark white headings (#FFFFFF), cool silver body (#9CA3AF), and
 * cobalt/indigo badges (#5B96F7 / #0F1E36).
 */
val MianuDarkColors = MianuColors(
    surface = Color(0xFF000000),
    surfaceRaised = Color(0xFF0E1116),
    surfaceSunken = Color(0xFF050608),
    surfaceOverlay = Color(0xFF14171E),
    surfaceHover = Color(0xFF1A1E27),
    border = Color(0xFF222631),
    fg = Color(0xFFFFFFFF),
    fgMuted = Color(0xFF9CA3AF),
    accent = Color(0xFFFFFFFF),
    goldAccent = Color(0xFFE2B755),
    success = Color(0xFF4ADE80),
    danger = Color(0xFFF87171),
    warning = Color(0xFFFBBF24),
    info = Color(0xFF60A5FA),
    committeeAccent = Color(0xFF5B96F7),
    committeeBg = Color(0xFF0F1E36),
    committeeBorder = Color(0xFF1E3A5F),
    isDark = true,
)

val MianuLightColors = MianuDarkColors.copy(
    // Retain black & white identity even if system reports light mode
    surface = Color(0xFF050608),
    surfaceRaised = Color(0xFF11141B),
    surfaceSunken = Color(0xFF000000),
    surfaceOverlay = Color(0xFF161922),
    surfaceHover = Color(0xFF1E222D),
    border = Color(0xFF252936),
    fg = Color(0xFFFFFFFF),
    fgMuted = Color(0xFFA1A1AA),
    accent = Color(0xFFFFFFFF),
    isDark = true,
)

// endregion

// Legacy flat aliases
val MianuSurfaceLight = MianuDarkColors.surface
val MianuSurfaceRaisedLight = MianuDarkColors.surfaceRaised
val MianuSurfaceSunkenLight = MianuDarkColors.surfaceSunken
val MianuBorderLight = MianuDarkColors.border
val MianuFgLight = MianuDarkColors.fg
val MianuFgMutedLight = MianuDarkColors.fgMuted
val MianuAccentLight = MianuDarkColors.accent
val MianuSuccessLight = MianuDarkColors.success
val MianuDangerLight = MianuDarkColors.danger
val MianuWarningLight = MianuDarkColors.warning

val MianuSurfaceDark = MianuDarkColors.surface
val MianuSurfaceRaisedDark = MianuDarkColors.surfaceRaised
val MianuSurfaceSunkenDark = MianuDarkColors.surfaceSunken
val MianuBorderDark = MianuDarkColors.border
val MianuFgDark = MianuDarkColors.fg
val MianuFgMutedDark = MianuDarkColors.fgMuted
val MianuAccentDark = MianuDarkColors.accent
val MianuSuccessDark = MianuDarkColors.success
val MianuDangerDark = MianuDarkColors.danger
val MianuWarningDark = MianuDarkColors.warning
