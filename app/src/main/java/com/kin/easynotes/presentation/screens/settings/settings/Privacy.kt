package com.kin.easynotes.presentation.screens.settings.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kin.easynotes.R
import com.kin.easynotes.presentation.navigation.NavRoutes
import com.kin.easynotes.presentation.screens.settings.SettingsScaffold
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import com.kin.easynotes.presentation.screens.settings.widgets.ActionType
import com.kin.easynotes.presentation.screens.settings.widgets.SettingsBox

@Composable
fun PrivacyScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val securityLevel = remember { settingsViewModel.keystoreManager.securityLevelDescription() }

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.privacy),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                // Hardware security level — informational
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.key_security_level),
                    description = securityLevel,
                    icon = Icons.Rounded.Shield,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isBoth = true),
                    actionType = ActionType.TEXT,
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.screen_protection),
                    description = stringResource(id = R.string.screen_protection_description),
                    icon = Icons.Filled.RemoveRedEye,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isBoth = true),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.screenProtection,
                    switchEnabled = {
                        settingsViewModel.update(settingsViewModel.settings.value.copy(screenProtection = it))
                    },
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "Biometric Unlock",
                    description = "Use fingerprint or device credentials to unlock",
                    icon = Icons.Rounded.Fingerprint,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isBoth = true),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.biometricUnlock,
                    switchEnabled = {
                        settingsViewModel.update(settingsViewModel.settings.value.copy(biometricUnlock = it))
                    },
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                // Auto-lock timeout selector
                val options = listOf(1, 5, 15, 30, 0)
                val labels  = listOf("1 min", "5 min", "15 min", "30 min", "Never")
                val currentIdx = options.indexOf(settingsViewModel.settings.value.autoLockMinutes)
                    .coerceAtLeast(0)

                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.auto_lock),
                    description = labels.getOrElse(currentIdx) { "5 min" },
                    icon = Icons.Rounded.LockClock,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isBoth = true),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        onExit()
                        val nextIdx = (currentIdx + 1) % options.size
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(autoLockMinutes = options[nextIdx])
                        )
                    },
                    customButton = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = "",
                            modifier = Modifier.scale(0.75f)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.change_password),
                    description = stringResource(id = R.string.change_password_description),
                    icon = Icons.Rounded.Key,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isBoth = true),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        onExit()
                        navController.navigate(NavRoutes.ChangePassword.route)
                    },
                    customButton = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = "",
                            modifier = Modifier.scale(0.75f)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
