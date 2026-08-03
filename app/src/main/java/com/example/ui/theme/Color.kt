package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

enum class AppColorTheme(val displayName: String, val chipBg: Color, val accentColor: Color) {
    ROYAL_GOLD("Royal Black & Gold", Color(0xFF1B1B1B), Color(0xFFDFB15B)),
    CRIMSON_FLAME("Crimson Ruby & Black", Color(0xFF1F0E11), Color(0xFFFF2E55)),
    EMERALD_LUSTRE("Emerald Green & Charcoal", Color(0xFF121F18), Color(0xFF00E676)),
    SAPPHIRE_ELITE("Sapphire Blue & Dark", Color(0xFF0B1527), Color(0xFF29B6F6)),
    ROSE_GOLD("Rose Gold & Dark Orchid", Color(0xFF230D2E), Color(0xFFFFB7B2))
}

// Global active app theme state
var currentAppTheme by mutableStateOf(AppColorTheme.ROYAL_GOLD)

// Premium Gold Palette - Backed by live properties
val PrimaryGold: Color
    get() = when (currentAppTheme) {
        AppColorTheme.ROYAL_GOLD -> Color(0xFFDFB15B)
        AppColorTheme.CRIMSON_FLAME -> Color(0xFFFF2E55)
        AppColorTheme.EMERALD_LUSTRE -> Color(0xFF00E676)
        AppColorTheme.SAPPHIRE_ELITE -> Color(0xFF29B6F6)
        AppColorTheme.ROSE_GOLD -> Color(0xFFFFB7B2)
    }

val LightGold: Color
    get() = when (currentAppTheme) {
        AppColorTheme.ROYAL_GOLD -> Color(0xFFF9E3B4)
        AppColorTheme.CRIMSON_FLAME -> Color(0xFFFF8A9F)
        AppColorTheme.EMERALD_LUSTRE -> Color(0xFFB9F6CA)
        AppColorTheme.SAPPHIRE_ELITE -> Color(0xFFB3E5FC)
        AppColorTheme.ROSE_GOLD -> Color(0xFFFFE5E3)
    }

val WarmGold: Color
    get() = when (currentAppTheme) {
        AppColorTheme.ROYAL_GOLD -> Color(0xFFC5A059)
        AppColorTheme.CRIMSON_FLAME -> Color(0xFFD32F2F)
        AppColorTheme.EMERALD_LUSTRE -> Color(0xFF00C853)
        AppColorTheme.SAPPHIRE_ELITE -> Color(0xFF0288D1)
        AppColorTheme.ROSE_GOLD -> Color(0xFFE5A19E)
    }

val DarkGold: Color
    get() = when (currentAppTheme) {
        AppColorTheme.ROYAL_GOLD -> Color(0xFF9E7E3C)
        AppColorTheme.CRIMSON_FLAME -> Color(0xFF9B001F)
        AppColorTheme.EMERALD_LUSTRE -> Color(0xFF004D20)
        AppColorTheme.SAPPHIRE_ELITE -> Color(0xFF01579B)
        AppColorTheme.ROSE_GOLD -> Color(0xFF904F4C)
    }

// Premium Obsidian/Dark Palette - Backed by live properties
val RichBlack: Color
    get() = when (currentAppTheme) {
        AppColorTheme.ROYAL_GOLD -> Color(0xFF0F0F0F)
        AppColorTheme.CRIMSON_FLAME -> Color(0xFF0D0204)
        AppColorTheme.EMERALD_LUSTRE -> Color(0xFF0A0F0D)
        AppColorTheme.SAPPHIRE_ELITE -> Color(0xFF020914)
        AppColorTheme.ROSE_GOLD -> Color(0xFF14001A)
    }

val SoftObsidian: Color
    get() = when (currentAppTheme) {
        AppColorTheme.ROYAL_GOLD -> Color(0xFF1B1B1B)
        AppColorTheme.CRIMSON_FLAME -> Color(0xFF1F0E11)
        AppColorTheme.EMERALD_LUSTRE -> Color(0xFF121F18)
        AppColorTheme.SAPPHIRE_ELITE -> Color(0xFF0B1527)
        AppColorTheme.ROSE_GOLD -> Color(0xFF230D2E)
    }

val DeepGray: Color
    get() = when (currentAppTheme) {
        AppColorTheme.ROYAL_GOLD -> Color(0xFF282828)
        AppColorTheme.CRIMSON_FLAME -> Color(0xFF321A1E)
        AppColorTheme.EMERALD_LUSTRE -> Color(0xFF1D3225)
        AppColorTheme.SAPPHIRE_ELITE -> Color(0xFF16253E)
        AppColorTheme.ROSE_GOLD -> Color(0xFF351641)
    }

val LightGray: Color
    get() = when (currentAppTheme) {
        AppColorTheme.ROYAL_GOLD -> Color(0xFFE5D3B3)
        AppColorTheme.CRIMSON_FLAME -> Color(0xFFF9D0D6)
        AppColorTheme.EMERALD_LUSTRE -> Color(0xFFC7EBCB)
        AppColorTheme.SAPPHIRE_ELITE -> Color(0xFFC3DEFC)
        AppColorTheme.ROSE_GOLD -> Color(0xFFFEE6FF)
    }

val CreamWhite: Color
    get() = when (currentAppTheme) {
        AppColorTheme.ROYAL_GOLD -> Color(0xFFFDFBF7)
        AppColorTheme.CRIMSON_FLAME -> Color(0xFFFFF9FA)
        AppColorTheme.EMERALD_LUSTRE -> Color(0xFFF5FFF6)
        AppColorTheme.SAPPHIRE_ELITE -> Color(0xFFF6FBFF)
        AppColorTheme.ROSE_GOLD -> Color(0xFFFFFAFF)
    }

// State Accents
val SuccessGreen = Color(0xFF2ECC71)      // Order Delivered / Payment Verified
val PendingAmber = Color(0xFFF1C40F)      // Pending order alert
val AlertRed = Color(0xFFE74C3C)          // Rejected/Cancelled order alert
