package com.quadlogixs.debugtool.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ExtraThemeColors(
    val disable: Color = Color.LightGray,
    val disableOutline: Color = Color.Gray,
    val onDisable: Color = Color.DarkGray,
    val tertiarySecondary: Color = Color(0xFF007AFF),
)

val LocalExtraThemeColors = staticCompositionLocalOf { ExtraThemeColors() }

@Composable
fun debugExtraColors(): ExtraThemeColors = LocalExtraThemeColors.current
