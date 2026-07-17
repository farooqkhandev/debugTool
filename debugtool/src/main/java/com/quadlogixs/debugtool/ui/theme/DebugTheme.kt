package com.quadlogixs.debugtool.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ExtraThemeColors(
    val disable: Color = Color(0xFFD1D1D6),
    val disableOutline: Color = Color(0xFFAEAEB2),
    val onDisable: Color = Color(0xFF636366),
    val tertiarySecondary: Color = Color(0xFF007AFF),
)

val LocalExtraThemeColors = staticCompositionLocalOf { ExtraThemeColors() }

@Composable
fun debugExtraColors(): ExtraThemeColors = LocalExtraThemeColors.current

/** Stable light palette for debug overlay UI — independent of the host app theme. */
private val DebugLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF2F2F7),
    onPrimaryContainer = Color(0xFF1C1C1E),
    secondary = Color(0xFF5856D6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5E5EA),
    onSecondaryContainer = Color(0xFF3A3A3C),
    tertiary = Color(0xFF34C759),
    onTertiary = Color.White,
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF3A3A3C),
    outline = Color(0xFFC7C7CC),
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E3),
)

/**
 * Material3 theme for **debug overlays only** (menu / dialogs).
 *
 * Never wrap host app [content] in this theme — that replaces the host primary color
 * (regression in the mis-tagged 1.0.5 dark release where teal overrode Login buttons).
 */
@Composable
fun DebugTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtraThemeColors provides ExtraThemeColors()) {
        MaterialTheme(
            colorScheme = DebugLightColorScheme,
            content = content,
        )
    }
}
