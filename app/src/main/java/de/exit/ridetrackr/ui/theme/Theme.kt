package de.exit.ridetrackr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

fun getAccentColorScheme(accent: String, isDark: Boolean): Pair<Color, Color> {
    val key = if (accent.equals("TEAL", ignoreCase = true)) "CYAN" else accent
    val palette = MaterialYouPalettes.find { it.id.equals(key, ignoreCase = true) }
        ?: MaterialYouPalettes.first()

    return if (isDark) {
        Pair(palette.bottomColor, palette.topColor)
    } else {
        Pair(palette.topColor, palette.bottomColor)
    }
}

@Composable
fun RideTrackrTheme(
    accent: String = "TEAL",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // When custom accent is chosen, disable dynamic color so the chosen accent is visible
    content: @Composable () -> Unit
) {
    val (primary, primaryContainer) = getAccentColorScheme(accent, darkTheme)

    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            primaryContainer = primaryContainer,
            onPrimaryContainer = Color.White,
            background = NeutralBackgroundDark,
            onBackground = NeutralOnBackgroundDark,
            surface = NeutralSurfaceDark,
            onSurface = NeutralOnSurfaceDark,
            surfaceContainer = Color(0xFF1E1E1E),
            surfaceContainerHigh = Color(0xFF262626)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = Color.Black,
            background = NeutralBackgroundLight,
            onBackground = NeutralOnBackgroundLight,
            surface = NeutralSurfaceLight,
            onSurface = NeutralOnSurfaceLight,
            surfaceContainer = Color(0xFFF3F5F4),
            surfaceContainerHigh = Color(0xFFEBEFED)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
