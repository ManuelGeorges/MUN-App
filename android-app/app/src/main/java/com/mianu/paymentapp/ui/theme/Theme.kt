package com.mianu.paymentapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Provides the active [MianuColors]. Read it via `MianuTheme.colors` rather than touching this
 * directly.
 */
val LocalMianuColors = staticCompositionLocalOf { MianuLightColors }

/** Accessors for the extended theme, mirroring how `MaterialTheme.colorScheme` reads. */
object MianuTheme {
    val colors: MianuColors
        @Composable @ReadOnlyComposable
        get() = LocalMianuColors.current
}

/**
 * Maps the semantic tokens onto Material's scheme so stock components (buttons, nav bars, text
 * fields) inherit the brand without every call site restating colors.
 *
 * Note the deliberate omission of dynamic color: MIANUCOM is a branded conference product, and
 * letting the device wallpaper repaint the accent would break the identity. The parameter is gone
 * rather than defaulted to false so it can't be switched on by accident.
 */
private fun materialSchemeFrom(c: MianuColors) = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0x22FFFFFF),
    onPrimaryContainer = Color.White,
    secondary = c.committeeAccent,
    onSecondary = Color.Black,
    secondaryContainer = c.committeeBg,
    onSecondaryContainer = c.committeeAccent,
    background = c.surface,
    onBackground = c.fg,
    surface = c.surfaceRaised,
    onSurface = c.fg,
    surfaceVariant = c.surfaceRaised,
    onSurfaceVariant = c.fgMuted,
    surfaceTint = Color.White,
    inverseSurface = c.fg,
    inverseOnSurface = c.surface,
    error = c.danger,
    onError = Color.Black,
    errorContainer = c.danger.copy(alpha = 0.16f),
    onErrorContainer = c.danger,
    outline = c.border,
    outlineVariant = c.border.copy(alpha = 0.6f),
    scrim = Color.Black.copy(alpha = 0.85f),
)

@Composable
fun MianuTheme(
    darkTheme: Boolean = true, // Default to conference black & white theme
    content: @Composable () -> Unit,
) {
    val mianuColors = if (darkTheme) MianuDarkColors else MianuLightColors
    val colorScheme = materialSchemeFrom(mianuColors)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Keep status and nav bars transparent over black constellation backdrop with white icons
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    CompositionLocalProvider(LocalMianuColors provides mianuColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
