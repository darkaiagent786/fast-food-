package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = darkColorScheme(
        primary = PrimaryGold,
        secondary = WarmGold,
        tertiary = LightGold,
        background = RichBlack,
        surface = SoftObsidian,
        onPrimary = RichBlack,
        onSecondary = CreamWhite,
        onTertiary = RichBlack,
        onBackground = CreamWhite,
        onSurface = CreamWhite,
        surfaceVariant = DeepGray,
        onSurfaceVariant = LightGray
    )
    MaterialTheme(
        colorScheme = dynamicColorScheme,
        typography = Typography,
        content = content
    )
}
