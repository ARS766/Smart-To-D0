package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AccentPalette(
    val displayName: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val hexString: String
) {
    BOLD_NEON("Bold Lime", BoldLime_Primary, BoldLime_Secondary, BoldLime_Tertiary, "#FFBCFE2F"),
    CYBER_PURPLE("Cyber Purple", CyberPurple_Primary, CyberPurple_Secondary, CyberPurple_Tertiary, "#FF8A2BE2"),
    MINTY_GREEN("Minty Green", MintyGreen_Primary, MintyGreen_Secondary, MintyGreen_Tertiary, "#FF00C896"),
    SUNSET_CORAL("Sunset Coral", SunsetCoral_Primary, SunsetCoral_Secondary, SunsetCoral_Tertiary, "#FFFF5E7E"),
    ELECTRIC_BLUE("Electric Blue", ElectricBlue_Primary, ElectricBlue_Secondary, ElectricBlue_Tertiary, "#FF0088FF")
}

@Composable
fun SmartReminderTheme(
    palette: AccentPalette = AccentPalette.BOLD_NEON,
    isDark: Boolean = true,
    isOled: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isDark -> {
            val bg = if (palette == AccentPalette.BOLD_NEON) BoldLime_Background else if (isOled) OLED_Background else Color(0xFF15151A)
            val surf = if (palette == AccentPalette.BOLD_NEON) BoldLime_Surface else if (isOled) OLED_Surface else Color(0xFF202026)
            val surfVar = if (palette == AccentPalette.BOLD_NEON) BoldLime_Border else if (isOled) Color(0xFF1A1A1E) else Color(0xFF282830)
            
            darkColorScheme(
                primary = palette.primary,
                secondary = palette.secondary,
                tertiary = palette.tertiary,
                background = bg,
                surface = surf,
                onBackground = OLED_OnBackground,
                onSurface = OLED_OnSurface,
                surfaceVariant = surfVar,
                onSurfaceVariant = Color(0xFFCCCCCC)
            )
        }
        else -> {
            lightColorScheme(
                primary = palette.primary,
                secondary = palette.secondary,
                tertiary = palette.tertiary,
                background = Light_Background,
                surface = Light_Surface,
                onBackground = Light_OnBackground,
                onSurface = Light_OnSurface,
                surfaceVariant = Color(0xFFEAEBED),
                onSurfaceVariant = Color(0xFF49454F)
            )
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
