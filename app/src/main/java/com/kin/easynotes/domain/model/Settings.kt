package com.kin.easynotes.domain.model

import com.kin.easynotes.presentation.navigation.NavRoutes

data class Settings(
    var defaultRouteType: String = NavRoutes.Home.route,
    // Single password hash — Argon2id, never plaintext.
    // null = first launch, password not yet set.
    var passwordHash: String? = null,
    // Auto-lock timeout in minutes. 0 = never (only lock when app is killed/restarted)
    var autoLockMinutes: Int = 5,
    // Brute-force protection: consecutive failed login attempts and the epoch-ms
    // timestamp (as String — the settings store only handles Boolean/String/Int)
    // until which login is refused. "0" = not locked out.
    var failedLoginAttempts: Int = 0,
    var lockoutUntil: String = "0",
    // These are written back by reflection in SettingsUseCase, so they must not be `val` —
    // final instance fields are not reliably settable and would silently never load.
    var viewMode: Boolean = true,
    var automaticTheme: Boolean = true,
    var darkTheme: Boolean = false,
    var dynamicTheme: Boolean = false,
    var amoledTheme: Boolean = false,
    var customColor: Int = -7896468,
    var minimalisticMode: Boolean = false,
    var extremeAmoledMode: Boolean = false,
    var isMarkdownEnabled: Boolean = true,
    var screenProtection: Boolean = true,
    var sortDescending: Boolean = true,
    var editMode: Boolean = false,
    var gallerySync: Boolean = true,
    var showOnlyTitle: Boolean = false,
    var termsOfService: Boolean = false,
    var useMonoSpaceFont: Boolean = false,
    var cornerRadius: Int = 32,
    var disableSwipeInEditMode: Boolean = false,
    var makeSearchBarLonger: Boolean = false,
    var fontSize: Int = 16,
    var biometricUnlock: Boolean = false,
    // Preset color palette. "system" keeps the existing dark/dynamic/amoled/customColor pipeline.
    // "catppuccin" and "dracula" fully override it (both are dark-only palettes by design).
    var themeMode: String = "system",
    var catppuccinAccent: String = "mauve",
    var draculaAccent: String = "purple",
    // "ptyxis" = 11 curated palettes (Ptyxis.kt), "ptyxis_ext" = 33 more (PtyxisPalettesExtended.kt)
    var ptyxisPalette: String = "nord",
    var ptyxisExtendedPalette: String = "solarized",
    // App-wide UI font family — see AppFont in presentation/theme/Fonts.kt
    var appFont: String = "nunito",
) {
    // True when password has been set up — app is ready to use
    val isSetup: Boolean get() = passwordHash != null
}
