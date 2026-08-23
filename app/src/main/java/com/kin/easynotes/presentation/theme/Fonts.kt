/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kin.easynotes.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.kin.easynotes.R

/** Bundled selectable font families. Each maps to a res/font *.ttf file. */
val nunitoFamily =
    FontFamily(
        Font(R.font.nunito_regular, FontWeight.Normal),
        Font(R.font.nunito_extralight, FontWeight.ExtraLight),
        Font(R.font.nunito_light, FontWeight.Light),
        Font(R.font.nunito_medium, FontWeight.Medium),
        Font(R.font.nunito_semibold, FontWeight.SemiBold),
        Font(R.font.nunito_bold, FontWeight.Bold),
        Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
        Font(R.font.nunito_black, FontWeight.Black),
    )

val cormorantGaramondFamily =
    FontFamily(
        Font(R.font.cormorant_garamond_bold, FontWeight.Bold),
        Font(R.font.cormorant_garamond_bolditalic, FontWeight.Bold, FontStyle.Italic),
        Font(R.font.cormorant_garamond_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.cormorant_garamond_light, FontWeight.Light),
        Font(R.font.cormorant_garamond_lightitalic, FontWeight.Light, FontStyle.Italic),
        Font(R.font.cormorant_garamond_medium, FontWeight.Medium),
        Font(R.font.cormorant_garamond_mediumitalic, FontWeight.Medium, FontStyle.Italic),
        Font(R.font.cormorant_garamond_regular, FontWeight.Normal),
        Font(R.font.cormorant_garamond_semibold, FontWeight.SemiBold),
        Font(R.font.cormorant_garamond_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    )

val dotgothic16Family =
    FontFamily(
        Font(R.font.dotgothic16_regular, FontWeight.Normal),
    )

val ibmPlexMonoFamily =
    FontFamily(
        Font(R.font.ibm_plex_mono_bold, FontWeight.Bold),
        Font(R.font.ibm_plex_mono_bolditalic, FontWeight.Bold, FontStyle.Italic),
        Font(R.font.ibm_plex_mono_extralight, FontWeight.ExtraLight),
        Font(R.font.ibm_plex_mono_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(R.font.ibm_plex_mono_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.ibm_plex_mono_light, FontWeight.Light),
        Font(R.font.ibm_plex_mono_lightitalic, FontWeight.Light, FontStyle.Italic),
        Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
        Font(R.font.ibm_plex_mono_mediumitalic, FontWeight.Medium, FontStyle.Italic),
        Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
        Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
        Font(R.font.ibm_plex_mono_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(R.font.ibm_plex_mono_thin, FontWeight.Thin),
        Font(R.font.ibm_plex_mono_thinitalic, FontWeight.Thin, FontStyle.Italic),
    )

val ibmPlexSerifFamily =
    FontFamily(
        Font(R.font.ibm_plex_serif_bold, FontWeight.Bold),
        Font(R.font.ibm_plex_serif_bolditalic, FontWeight.Bold, FontStyle.Italic),
        Font(R.font.ibm_plex_serif_extralight, FontWeight.ExtraLight),
        Font(R.font.ibm_plex_serif_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(R.font.ibm_plex_serif_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.ibm_plex_serif_light, FontWeight.Light),
        Font(R.font.ibm_plex_serif_lightitalic, FontWeight.Light, FontStyle.Italic),
        Font(R.font.ibm_plex_serif_medium, FontWeight.Medium),
        Font(R.font.ibm_plex_serif_mediumitalic, FontWeight.Medium, FontStyle.Italic),
        Font(R.font.ibm_plex_serif_regular, FontWeight.Normal),
        Font(R.font.ibm_plex_serif_semibold, FontWeight.SemiBold),
        Font(R.font.ibm_plex_serif_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(R.font.ibm_plex_serif_thin, FontWeight.Thin),
        Font(R.font.ibm_plex_serif_thinitalic, FontWeight.Thin, FontStyle.Italic),
    )

val instrumentSerifFamily =
    FontFamily(
        Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.instrument_serif_regular, FontWeight.Normal),
    )

val notoSansEgyptianHieroglyphsFamily =
    FontFamily(
        Font(R.font.noto_sans_egyptian_hieroglyphs_regular, FontWeight.Normal),
    )

val playfairDisplayFamily =
    FontFamily(
        Font(R.font.playfair_display_black, FontWeight.Black),
        Font(R.font.playfair_display_blackitalic, FontWeight.Black, FontStyle.Italic),
        Font(R.font.playfair_display_bold, FontWeight.Bold),
        Font(R.font.playfair_display_bolditalic, FontWeight.Bold, FontStyle.Italic),
        Font(R.font.playfair_display_extrabold, FontWeight.ExtraBold),
        Font(R.font.playfair_display_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
        Font(R.font.playfair_display_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.playfair_display_medium, FontWeight.Medium),
        Font(R.font.playfair_display_mediumitalic, FontWeight.Medium, FontStyle.Italic),
        Font(R.font.playfair_display_regular, FontWeight.Normal),
        Font(R.font.playfair_display_semibold, FontWeight.SemiBold),
        Font(R.font.playfair_display_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    )

val pressStart2pFamily =
    FontFamily(
        Font(R.font.press_start_2p_regular, FontWeight.Normal),
    )

val quicksandFamily =
    FontFamily(
        Font(R.font.quicksand_bold, FontWeight.Bold),
        Font(R.font.quicksand_light, FontWeight.Light),
        Font(R.font.quicksand_medium, FontWeight.Medium),
        Font(R.font.quicksand_regular, FontWeight.Normal),
        Font(R.font.quicksand_semibold, FontWeight.SemiBold),
    )

val spaceGroteskFamily =
    FontFamily(
        Font(R.font.space_grotesk_bold, FontWeight.Bold),
        Font(R.font.space_grotesk_light, FontWeight.Light),
        Font(R.font.space_grotesk_medium, FontWeight.Medium),
        Font(R.font.space_grotesk_regular, FontWeight.Normal),
        Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
    )

val turretRoadFamily =
    FontFamily(
        Font(R.font.turret_road_bold, FontWeight.Bold),
        Font(R.font.turret_road_extrabold, FontWeight.ExtraBold),
        Font(R.font.turret_road_extralight, FontWeight.ExtraLight),
        Font(R.font.turret_road_light, FontWeight.Light),
        Font(R.font.turret_road_medium, FontWeight.Medium),
        Font(R.font.turret_road_regular, FontWeight.Normal),
    )

val viaodaLibreFamily =
    FontFamily(
        Font(R.font.viaoda_libre_regular, FontWeight.Normal),
    )

/** User-selectable app font. NUNITO is the default (matches the previous system-default look). */
enum class AppFont(val key: String, val displayName: String, val family: FontFamily) {
    NUNITO("nunito", "Nunito (default)", nunitoFamily),
    CORMORANT_GARAMOND("cormorant_garamond", "Cormorant Garamond", cormorantGaramondFamily),
    DOTGOTHIC16("dotgothic16", "DotGothic16", dotgothic16Family),
    IBM_PLEX_MONO("ibm_plex_mono", "IBM Plex Mono", ibmPlexMonoFamily),
    IBM_PLEX_SERIF("ibm_plex_serif", "IBM Plex Serif", ibmPlexSerifFamily),
    INSTRUMENT_SERIF("instrument_serif", "Instrument Serif", instrumentSerifFamily),
    NOTO_SANS_EGYPTIAN_HIEROGLYPHS("noto_sans_egyptian_hieroglyphs", "Egyptian Hieroglyphs", notoSansEgyptianHieroglyphsFamily),
    PLAYFAIR_DISPLAY("playfair_display", "Playfair Display", playfairDisplayFamily),
    PRESS_START_2P("press_start_2p", "Press Start 2P", pressStart2pFamily),
    QUICKSAND("quicksand", "Quicksand", quicksandFamily),
    SPACE_GROTESK("space_grotesk", "Space Grotesk", spaceGroteskFamily),
    TURRET_ROAD("turret_road", "Turret Road", turretRoadFamily),
    VIAODA_LIBRE("viaoda_libre", "Viaoda Libre", viaodaLibreFamily),
}

fun appFontFromKey(key: String): AppFont =
    AppFont.entries.firstOrNull { it.key == key } ?: AppFont.NUNITO

private val baselineTypography = Typography()

/** Builds Typography with [fontFamily] applied to every text role; sizes are left untouched — this app scales sizes separately via the fontSize setting (see FontUtils). */
fun buildAppTypography(fontFamily: FontFamily): Typography =
    Typography(
        displayLarge = baselineTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = baselineTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = baselineTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = baselineTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = baselineTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = baselineTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = baselineTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = baselineTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = baselineTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = baselineTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = baselineTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = baselineTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = baselineTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = baselineTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = baselineTypography.labelSmall.copy(fontFamily = fontFamily),
    )
