package com.kin.easynotes.presentation

import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import com.kin.easynotes.presentation.navigation.AppNavHost
import com.kin.easynotes.presentation.navigation.NavRoutes
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import com.kin.easynotes.presentation.theme.NotesTheme
import dagger.hilt.android.AndroidEntryPoint

fun NavOptionsBuilder.popUpToTop(navController: NavController) {
    popUpTo(navController.currentBackStackEntry?.destination?.route ?: return) {
        inclusive = true
    }
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavHostController
    private var settingsViewModel: SettingsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            settingsViewModel = hiltViewModel<SettingsViewModel>()

            if (settingsViewModel!!.settings.value.screenProtection) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            val rawNoteId = intent?.getIntExtra("noteId", -1) ?: -1

            if (settingsViewModel!!.settings.value.gallerySync) {
                contentResolver.registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    true,
                    settingsViewModel!!.galleryObserver
                )
            }

            NotesTheme(settingsViewModel!!) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    navController = rememberNavController()

                    // Security: if app is locked and a widget tap delivered a noteId,
                    // store it as pending — LoginScreen will navigate to it after auth
                    val isLocked = settingsViewModel!!.settings.value.isSetup
                    val safeNoteId = if (isLocked && rawNoteId != -1) {
                        settingsViewModel!!.pendingWidgetNoteId = rawNoteId
                        -1
                    } else {
                        rawNoteId
                    }

                    AppNavHost(
                        settingsViewModel!!,
                        navController,
                        safeNoteId,
                        settingsViewModel!!.defaultRoute!!
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-lock when app resumes if session password is gone (e.g. process killed)
        settingsViewModel?.let {
            if (it.settings.value.isSetup && it.sessionPassword == null) {
                if (::navController.isInitialized) {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}
