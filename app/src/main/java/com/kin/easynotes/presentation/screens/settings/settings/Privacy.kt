package com.kin.easynotes.presentation.screens.settings.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.privacy),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
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
