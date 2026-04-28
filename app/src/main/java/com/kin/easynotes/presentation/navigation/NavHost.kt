package com.kin.easynotes.presentation.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
    noteId: Int,
    defaultRoute: String
) {
    val activity = (LocalContext.current as? Activity)

    // Security: if app is locked and a widget delivered a noteId, route to login first
    val startRoute = when {
        defaultRoute == NavRoutes.Login.route && noteId != -1 -> NavRoutes.Login.route
        defaultRoute == NavRoutes.PasswordSetup.route -> NavRoutes.PasswordSetup.route
        noteId != -1 -> NavRoutes.Edit.createRoute(noteId, true)
        else -> defaultRoute
    }

    NavHost(navController, startDestination = startRoute) {

        animatedComposable(NavRoutes.PasswordSetup.route) {
            PasswordSetupScreen(
                settingsViewModel = settingsModel,
                navController = navController,
                isChangingPassword = false
            )
        }

        animatedComposable(NavRoutes.ChangePassword.route) {
            PasswordSetupScreen(
                settingsViewModel = settingsModel,
                navController = navController,
                isChangingPassword = true
            )
        }

        animatedComposable(NavRoutes.Login.route) {
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
                id = if (noteId == -1) id else noteId,
                encrypted = true,
                isWidget = noteId != -1
            ) {
                if (noteId == -1) navController.navigateUp()
                else activity?.finish()
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
