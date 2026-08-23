package com.kin.easynotes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.kin.easynotes.presentation.screens.edit.EditNoteView
import com.kin.easynotes.presentation.screens.home.HomeView
import com.kin.easynotes.presentation.screens.login.LoginScreen
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import com.kin.easynotes.presentation.screens.setup.PasswordSetupScreen

@Composable
fun AppNavHost(
    settingsModel: SettingsViewModel,
    navController: NavHostController = rememberNavController(),
    defaultRoute: String
) {
    NavHost(navController, startDestination = defaultRoute) {

        fadeThroughComposable(NavRoutes.PasswordSetup.route) {
            PasswordSetupScreen(
                settingsViewModel = settingsModel,
                navController = navController,
                isChangingPassword = false
            )
        }

        fadeThroughComposable(NavRoutes.ChangePassword.route) {
            PasswordSetupScreen(
                settingsViewModel = settingsModel,
                navController = navController,
                isChangingPassword = true
            )
        }

        fadeThroughComposable(NavRoutes.Login.route) {
            LoginScreen(
                settingsViewModel = settingsModel,
                navController = navController
            )
        }


        animatedComposable(NavRoutes.Home.route) {
            HomeView(
                onSettingsClicked = { navController.navigate(NavRoutes.Settings.route) },
                onNoteClicked = { id ->
                    navController.navigate(NavRoutes.Edit.createRoute(id, true))
                },
                settingsModel = settingsModel
            )
        }

        animatedComposable(NavRoutes.Edit.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
            EditNoteView(
                settingsViewModel = settingsModel,
                id = id,
                encrypted = true
            ) {
                navController.navigateUp()
            }
        }

        settingScreens.forEach { (route, screen) ->
            if (route == NavRoutes.Settings.route) {
                slideInComposable(route) { screen(settingsModel, navController) }
            } else {
                animatedComposable(route) { screen(settingsModel, navController) }
            }
        }
    }
}
