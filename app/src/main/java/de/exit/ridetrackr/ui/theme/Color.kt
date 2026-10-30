package de.exit.ridetrackr.ui.theme

import androidx.compose.ui.graphics.Color

data class M3ThemePalette(
    val id: String,
    val name: String,
    val topColor: Color,
    val bottomColor: Color
)

val MaterialYouPalettes = listOf(
    M3ThemePalette("BLUE", "Kobaltblau", Color(0xFF4150AF), Color(0xFFACCBFA)),
    M3ThemePalette("PURPLE", "Violett", Color(0xFF68548F), Color(0xFFD2BCFD)),
    M3ThemePalette("PINK", "Beere", Color(0xFF874A69), Color(0xFFFDB0D4)),
    M3ThemePalette("CORAL", "Koralle", Color(0xFF904A42), Color(0xFFFEB3AD)),
    M3ThemePalette("ORANGE", "Rostorange", Color(0xFFA83901), Color(0xFFFEB781)),
    M3ThemePalette("AMBER", "Ocker", Color(0xFF7C580C), Color(0xFFEFBF6D)),
    M3ThemePalette("OLIVE", "Olivgrün", Color(0xFF5C631D), Color(0xFFC5CD7A)),
    M3ThemePalette("GREEN", "Waldgrün", Color(0xFF39683A), Color(0xFF9FD49C)),
    M3ThemePalette("CYAN", "Ozean-Teal", Color(0xFF016877), Color(0xFF84D3E2)),
    M3ThemePalette("CHARCOAL", "Schiefergrau", Color(0xFF3E484A), Color(0xFF7C8688))
)

// Neutral Surfaces
val NeutralBackgroundLight = Color(0xFFFAFDFB)
val NeutralOnBackgroundLight = Color(0xFF191C1C)
val NeutralSurfaceLight = Color(0xFFFAFDFB)
val NeutralOnSurfaceLight = Color(0xFF191C1C)

val NeutralBackgroundDark = Color(0xFF121212)
val NeutralOnBackgroundDark = Color(0xFFE0E3E1)
val NeutralSurfaceDark = Color(0xFF121212)
val NeutralOnSurfaceDark = Color(0xFFE0E3E1)
