package com.kin.easynotes.domain.model

import com.kin.easynotes.presentation.navigation.NavRoutes

data class Settings(
    var defaultRouteType: String = NavRoutes.Home.route,
    // Single password hash — Argon2id, never plaintext.
    // null = first launch, password not yet set.
    var passwordHash: String? = null,
    // Auto-lock timeout in minutes. 0 = never (only lock when app is killed/restarted)
    var autoLockMinutes: Int = 5,
    val viewMode: Boolean = true,
    val automaticTheme: Boolean = true,
    val darkTheme: Boolean = false,
    var dynamicTheme: Boolean = false,
    var amoledTheme: Boolean = false,
    var customColor: Int = -7896468,
    var minimalisticMode: Boolean = false,
    var extremeAmoledMode: Boolean = false,
    var isMarkdownEnabled: Boolean = true,
    var screenProtection: Boolean = false,
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
    var biometricUnlock: Boolean = false
) {
    // True when password has been set up — app is ready to use
    val isSetup: Boolean get() = passwordHash != null
}
