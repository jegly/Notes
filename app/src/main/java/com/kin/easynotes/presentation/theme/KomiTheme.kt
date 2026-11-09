package com.kin.easynotes.presentation.theme

import androidx.compose.foundation.border
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme

/**
 * "KOMI" theme: a vintage paper-and-ink look (cream pages, near-black ink, hard square corners,
 * thick borders, flat offset shadows, mono/pixel type). Selected with themeMode == "komi".
 */
const val THEME_MODE_KOMI = "komi"

/** Set from [NotesTheme] so non-composable helpers (e.g. shapeManager) can square off corners. */
object KomiState {
    @Volatile var active: Boolean = false
}

/** Squares off any rounded shape while the KOMI theme is active. */
fun komiShape(shape: RoundedCornerShape): RoundedCornerShape =
    if (KomiState.active) RoundedCornerShape(0.dp) else shape

private object KomiPalette {
    val DayPage = Color(0xFFF1EADC)
    val DayPanel = Color(0xFFFAF5EA)
    val DayWell = Color(0xFFE7DEC9)
    val DayInk = Color(0xFF1B150D)
    val DayMuted = Color(0xFF695F50)

    val NightPage = Color(0xFF0C0A07)
    val NightPanel = Color(0xFF16120C)
    val NightWell = Color(0xFF211B12)
    val NightInk = Color(0xFFF0E9DA)
    val NightMuted = Color(0xFF968B77)
}

fun komiColorScheme(dark: Boolean): ColorScheme {
    val p = KomiPalette
    return if (!dark) {
        lightColorScheme(
            primary = Color(0xFFC8402B), onPrimary = p.DayPanel,
            primaryContainer = Color(0xFFF2CFC4), onPrimaryContainer = p.DayInk,
            secondary = Color(0xFF2B4C7E), onSecondary = p.DayPanel,
            secondaryContainer = Color(0xFFCFD9E8), onSecondaryContainer = p.DayInk,
            tertiary = Color(0xFF9A6B00), onTertiary = p.DayPanel,
            tertiaryContainer = Color(0xFFEBD9A8), onTertiaryContainer = p.DayInk,
            error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF4CFCB), onErrorContainer = Color(0xFF410E0B),
            background = p.DayPage, onBackground = p.DayInk,
            surface = p.DayPage, onSurface = p.DayInk,
            surfaceVariant = p.DayWell, onSurfaceVariant = p.DayMuted,
            outline = p.DayInk, outlineVariant = Color(0xFFCFC4AB),
            scrim = Color(0xFF000000),
            inverseSurface = p.DayInk, inverseOnSurface = p.DayPage, inversePrimary = Color(0xFFE5604A),
            surfaceDim = p.DayWell, surfaceBright = p.DayPanel,
            surfaceContainerLowest = p.DayPage,
            surfaceContainerLow = p.DayPanel,
            surfaceContainer = p.DayPanel,
            surfaceContainerHigh = p.DayWell,
            surfaceContainerHighest = Color(0xFFDDD2BA),
        )
    } else {
        darkColorScheme(
            primary = Color(0xFFE5604A), onPrimary = Color(0xFF1B150D),
            primaryContainer = Color(0xFF4A2219), onPrimaryContainer = p.NightInk,
            secondary = Color(0xFF7FA0D6), onSecondary = Color(0xFF0C0A07),
            secondaryContainer = Color(0xFF1F3150), onSecondaryContainer = p.NightInk,
            tertiary = Color(0xFFE0B341), onTertiary = Color(0xFF0C0A07),
            tertiaryContainer = Color(0xFF4A3A0C), onTertiaryContainer = p.NightInk,
            error = Color(0xFFFF6B5E), onError = Color(0xFF1B150D),
            errorContainer = Color(0xFF5A1F1A), onErrorContainer = Color(0xFFFFDAD6),
            background = p.NightPage, onBackground = p.NightInk,
            surface = p.NightPage, onSurface = p.NightInk,
            surfaceVariant = p.NightWell, onSurfaceVariant = p.NightMuted,
            outline = p.NightInk, outlineVariant = Color(0xFF3A3225),
            scrim = Color(0xFF000000),
            inverseSurface = p.NightInk, inverseOnSurface = p.NightPage, inversePrimary = Color(0xFFC8402B),
            surfaceDim = p.NightPage, surfaceBright = p.NightWell,
            surfaceContainerLowest = p.NightPage,
            surfaceContainerLow = p.NightPanel,
            surfaceContainer = p.NightPanel,
            surfaceContainerHigh = p.NightWell,
            surfaceContainerHighest = Color(0xFF2B2418),
        )
    }
}

val KomiShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

/** Pixel font for headings, mono for everything else. */
fun komiTypography(): Typography {
    val base = Typography()
    val mono: FontFamily = ibmPlexMonoFamily
    val pixel: FontFamily = pressStart2pFamily
    fun TextStyle.mono() = copy(fontFamily = mono)
    fun TextStyle.pixel(size: Int) = copy(fontFamily = pixel, fontSize = size.sp, lineHeight = (size * 1.6f).sp, letterSpacing = 0.sp)
    return Typography(
        displayLarge = base.displayLarge.pixel(28),
        displayMedium = base.displayMedium.pixel(24),
        displaySmall = base.displaySmall.pixel(20),
        headlineLarge = base.headlineLarge.pixel(18),
        headlineMedium = base.headlineMedium.pixel(16),
        headlineSmall = base.headlineSmall.pixel(14),
        titleLarge = base.titleLarge.pixel(14),
        titleMedium = base.titleMedium.mono(),
        titleSmall = base.titleSmall.mono(),
        bodyLarge = base.bodyLarge.mono(),
        bodyMedium = base.bodyMedium.mono(),
        bodySmall = base.bodySmall.mono(),
        labelLarge = base.labelLarge.mono(),
        labelMedium = base.labelMedium.mono(),
        labelSmall = base.labelSmall.mono(),
    )
}

/** Thick ink border; no-op outside KOMI. */
fun Modifier.komiBorder(shape: Shape = RoundedCornerShape(0.dp)): Modifier =
    if (!KomiState.active) this else composed { border(2.dp, MaterialTheme.colorScheme.outline, shape) }

/** Flat, zero-blur offset shadow (manga panel style); no-op outside KOMI. Place before clip/background. */
fun Modifier.komiShadow(): Modifier =
    if (!KomiState.active) this else composed {
        val c = MaterialTheme.colorScheme.outline
        drawBehind {
            val o = 4.dp.toPx()
            drawRect(c, topLeft = Offset(o, o), size = Size(size.width, size.height))
        }
    }

/** Faint grid-paper background for whole screens (incl. the lock screen); no-op outside KOMI. */
fun Modifier.komiGridPaper(): Modifier =
    if (!KomiState.active) this else composed {
        val c = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f)
        drawBehind {
            val step = 24.dp.toPx()
            var x = 0f
            while (x < size.width) { drawLine(c, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
            var y = 0f
            while (y < size.height) { drawLine(c, Offset(0f, y), Offset(size.width, y), 1f); y += step }
        }
    }
