package com.quadlogixs.debugtool.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Canonical dark palette for the debugTool library UI.
 * Host-app light themes must not leak into debug overlays.
 */
object DebugColors {
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1A1A1A)
    val SurfaceElevated = Color(0xFF242424)
    val Border = Color(0xFF2C2C2E)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFABABAB)
    val AccentTeal = Color(0xFF26A69A)
    val AccentBlue = Color(0xFF4285F4)
    val AccentPurple = Color(0xFFB388FF)
    val AccentOrange = Color(0xFFFF9800)
    val AccentGreen = Color(0xFF69F0AE)
    val AccentRed = Color(0xFFFF5252)
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFC107)
    val OnAccent = Color(0xFF121212)
    val ChipVerbose = Color(0xFFE0E0E0)
    val ChipDebug = AccentGreen
    val ChipInfo = AccentBlue
    val ChipWarn = Warning
    val ChipError = AccentRed
}

data class ExtraThemeColors(
    val disable: Color = DebugColors.SurfaceElevated,
    val disableOutline: Color = DebugColors.Border,
    val onDisable: Color = DebugColors.TextSecondary,
    val tertiarySecondary: Color = DebugColors.AccentTeal,
)

val LocalExtraThemeColors = staticCompositionLocalOf {
    ExtraThemeColors()
}

@Composable
fun debugExtraColors(): ExtraThemeColors = LocalExtraThemeColors.current

private val DebugDarkColorScheme = darkColorScheme(
    primary = DebugColors.AccentTeal,
    onPrimary = DebugColors.OnAccent,
    primaryContainer = DebugColors.SurfaceElevated,
    onPrimaryContainer = DebugColors.TextPrimary,
    secondary = DebugColors.AccentTeal,
    onSecondary = DebugColors.OnAccent,
    secondaryContainer = DebugColors.SurfaceElevated,
    onSecondaryContainer = DebugColors.TextSecondary,
    tertiary = DebugColors.AccentPurple,
    onTertiary = DebugColors.TextPrimary,
    tertiaryContainer = DebugColors.SurfaceElevated,
    onTertiaryContainer = DebugColors.TextPrimary,
    background = DebugColors.Background,
    onBackground = DebugColors.TextPrimary,
    surface = DebugColors.Surface,
    onSurface = DebugColors.TextPrimary,
    surfaceVariant = DebugColors.SurfaceElevated,
    onSurfaceVariant = DebugColors.TextSecondary,
    outline = DebugColors.Border,
    outlineVariant = DebugColors.Border,
    error = DebugColors.AccentRed,
    onError = DebugColors.TextPrimary,
    errorContainer = Color(0xFF3D1515),
    onErrorContainer = DebugColors.AccentRed,
)

/**
 * Applies the debugTool dark Material3 scheme + [LocalExtraThemeColors].
 * Wrap every debug UI entry point (scaffold, bottom sheet, dialogs).
 */
@Composable
fun DebugToolTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalExtraThemeColors provides ExtraThemeColors(),
    ) {
        MaterialTheme(
            colorScheme = DebugDarkColorScheme,
            content = content,
        )
    }
}

/** Dark extra colors even if somehow composed outside [DebugToolTheme]. */
@Composable
fun rememberDebugExtraColors(
    darkTheme: Boolean = isSystemInDarkTheme(),
): ExtraThemeColors = ExtraThemeColors(
    disable = if (darkTheme) DebugColors.SurfaceElevated else Color.LightGray,
    disableOutline = if (darkTheme) DebugColors.Border else Color.Gray,
    onDisable = if (darkTheme) DebugColors.TextSecondary else Color.DarkGray,
    tertiarySecondary = if (darkTheme) DebugColors.AccentTeal else Color(0xFF007AFF),
)
