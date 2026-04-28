package com.kin.easynotes.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel

// Support screen removed — this is a private personal build.
@Composable
fun SupportContent(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    onExit: () -> Unit
) {
    onExit()
}
