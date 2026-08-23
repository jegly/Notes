package com.kin.easynotes.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

object CatppuccinMocha {
    val Base       = Color(0xFF1E1E2E)
    val Mantle     = Color(0xFF181825)
    val Crust      = Color(0xFF11111B)
    val Surface0   = Color(0xFF313244)
    val Surface1   = Color(0xFF45475A)
    val Overlay0   = Color(0xFF6C7086)
    val Subtext0   = Color(0xFFA6ADC8)
    val Text       = Color(0xFFCDD6F4)

    val Rosewater  = Color(0xFFF5E0DC)
    val Flamingo   = Color(0xFFF2CDCD)
    val Pink       = Color(0xFFF38BA8)
    val Mauve      = Color(0xFFCBA6F7)
    val Maroon     = Color(0xFFEBA0AC)
    val Peach      = Color(0xFFFAB387)
    val Yellow     = Color(0xFFF9E2AF)
    val Green      = Color(0xFFA6E3A1)
    val Teal       = Color(0xFF94E2D5)
    val Sky        = Color(0xFF89DCEB)
    val Sapphire   = Color(0xFF74C7EC)
    val Blue       = Color(0xFF89B4FA)
    val Lavender   = Color(0xFFB4BEFE)

    val Secondary  = Blue
    val Tertiary   = Peach

    val accents: Map<String, Pair<String, Color>> = linkedMapOf(
        "mauve"     to ("Mauve"     to Mauve),
        "blue"      to ("Blue"      to Blue),
        "lavender"  to ("Lavender"  to Lavender),
        "sapphire"  to ("Sapphire"  to Sapphire),
        "sky"       to ("Sky"       to Sky),
        "teal"      to ("Teal"      to Teal),
        "green"     to ("Green"     to Green),
        "yellow"    to ("Yellow"    to Yellow),
        "peach"     to ("Peach"     to Peach),
        "maroon"    to ("Maroon"    to Maroon),
        "pink"      to ("Pink"      to Pink),
        "flamingo"  to ("Flamingo"  to Flamingo),
        "rosewater" to ("Rosewater" to Rosewater),
    )
}

object DraculaColors {
    val Background  = Color(0xFF282A36)
    val DarkerBg    = Color(0xFF21222C)
    val CurrentLine = Color(0xFF44475A)
    val Foreground  = Color(0xFFF8F8F2)
    val Comment     = Color(0xFF6272A4)

    val Purple  = Color(0xFFBD93F9)
    val Pink    = Color(0xFFFF79C6)
    val Cyan    = Color(0xFF8BE9FD)
    val Green   = Color(0xFF50FA7B)
    val Orange  = Color(0xFFFFB86C)
    val Red     = Color(0xFFFF5555)
    val Yellow  = Color(0xFFF1FA8C)

    val Secondary = Cyan
    val Tertiary  = Pink

    val accents: Map<String, Pair<String, Color>> = linkedMapOf(
        "purple" to ("Purple" to Purple),
        "pink"   to ("Pink"   to Pink),
        "cyan"   to ("Cyan"   to Cyan),
        "green"  to ("Green"  to Green),
        "orange" to ("Orange" to Orange),
        "red"    to ("Red"    to Red),
        "yellow" to ("Yellow" to Yellow),
    )
}

fun catppuccinColorScheme(accentKey: String): ColorScheme {
    val accent    = CatppuccinMocha.accents[accentKey]?.second ?: CatppuccinMocha.Mauve
    val bg        = CatppuccinMocha.Base
    val secondary = CatppuccinMocha.Secondary
    val tertiary  = CatppuccinMocha.Tertiary
    fun tint(c: Color) = lerp(bg, c, 0.22f)

    return darkColorScheme(
        primary               = accent,
        onPrimary             = CatppuccinMocha.Crust,
        primaryContainer      = tint(accent),
        onPrimaryContainer    = accent,
        secondary             = secondary,
        onSecondary           = CatppuccinMocha.Crust,
        secondaryContainer    = tint(secondary),
        onSecondaryContainer  = secondary,
        tertiary              = tertiary,
        onTertiary            = CatppuccinMocha.Crust,
        tertiaryContainer     = tint(tertiary),
        onTertiaryContainer   = tertiary,
        background            = bg,
        onBackground          = CatppuccinMocha.Text,
        surface               = CatppuccinMocha.Mantle,
        onSurface             = CatppuccinMocha.Text,
        surfaceVariant        = CatppuccinMocha.Surface0,
        onSurfaceVariant      = CatppuccinMocha.Subtext0,
        surfaceContainerLowest = CatppuccinMocha.Crust,
        surfaceContainerLow   = CatppuccinMocha.Mantle,
        surfaceContainer      = CatppuccinMocha.Surface0,
        surfaceContainerHigh  = tint(CatppuccinMocha.Surface1),
        surfaceContainerHighest = CatppuccinMocha.Surface1,
        outline               = CatppuccinMocha.Overlay0,
        outlineVariant        = CatppuccinMocha.Surface1,
        error                 = CatppuccinMocha.Pink,
        onError               = CatppuccinMocha.Crust,
        errorContainer        = tint(CatppuccinMocha.Pink),
        onErrorContainer      = CatppuccinMocha.Pink,
        scrim                 = CatppuccinMocha.Crust,
        inverseSurface        = CatppuccinMocha.Text,
        inverseOnSurface      = bg,
        inversePrimary        = lerp(CatppuccinMocha.Text, accent, 0.5f),
    )
}

fun draculaColorScheme(accentKey: String): ColorScheme {
    val accent    = DraculaColors.accents[accentKey]?.second ?: DraculaColors.Purple
    val bg        = DraculaColors.Background
    val secondary = DraculaColors.Secondary
    val tertiary  = DraculaColors.Tertiary
    fun tint(c: Color) = lerp(bg, c, 0.22f)

    return darkColorScheme(
        primary               = accent,
        onPrimary             = bg,
        primaryContainer      = tint(accent),
        onPrimaryContainer    = accent,
        secondary             = secondary,
        onSecondary           = bg,
        secondaryContainer    = tint(secondary),
        onSecondaryContainer  = secondary,
        tertiary              = tertiary,
        onTertiary            = bg,
        tertiaryContainer     = tint(tertiary),
        onTertiaryContainer   = tertiary,
        background            = bg,
        onBackground          = DraculaColors.Foreground,
        surface               = DraculaColors.DarkerBg,
        onSurface             = DraculaColors.Foreground,
        surfaceVariant        = DraculaColors.CurrentLine,
        onSurfaceVariant      = DraculaColors.Comment,
        surfaceContainerLowest = Color(0xFF191A21),
        surfaceContainerLow   = DraculaColors.DarkerBg,
        surfaceContainer      = bg,
        surfaceContainerHigh  = DraculaColors.CurrentLine,
        surfaceContainerHighest = lerp(DraculaColors.CurrentLine, DraculaColors.Foreground, 0.08f),
        outline               = DraculaColors.Comment,
        outlineVariant        = DraculaColors.CurrentLine,
        error                 = DraculaColors.Red,
        onError               = bg,
        errorContainer        = tint(DraculaColors.Red),
        onErrorContainer      = DraculaColors.Red,
        scrim                 = Color(0xFF191A21),
        inverseSurface        = DraculaColors.Foreground,
        inverseOnSurface      = bg,
        inversePrimary        = lerp(DraculaColors.Foreground, accent, 0.5f),
    )
}
