package com.kin.easynotes.presentation

import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kin.easynotes.presentation.components.unregisterGalleryObserver
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import com.kin.easynotes.BuildConfig
import com.kin.easynotes.R
import com.kin.easynotes.presentation.navigation.AppNavHost
import com.kin.easynotes.presentation.navigation.NavRoutes
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import com.kin.easynotes.presentation.theme.NotesTheme
import com.kin.easynotes.security.SignatureVerifier
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

        if (!BuildConfig.DEBUG && !SignatureVerifier(this).isSignedByTrustedCert()) {
            showTamperDialog()
            return
        }

        enableEdgeToEdge()

        // Opt the whole window out of autofill.
        //
        // Android hands a text field's entire contents to the autofill framework on every
        // value change, over Binder, whose transaction limit is about 1MB. Pasting a large
        // note therefore killed the process outright:
        //
        //   RuntimeException: android.os.TransactionTooLargeException: data parcel size 514720
        //     at android.view.autofill.AutofillManager.notifyValueChanged
        //
        // Independently of the crash, an encrypted notes app has no business handing note
        // text to whatever autofill service happens to be installed. There is nothing here
        // autofill can usefully complete.
        window.decorView.importantForAutofill =
            View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS

        setContent {
            settingsViewModel = hiltViewModel<SettingsViewModel>()
            val vm = settingsViewModel!!

            // Was applied directly in the composable body: it re-ran on every recomposition
            // and, worse, was never cleared — turning screen protection off left FLAG_SECURE
            // set until the process died.
            val screenProtection = vm.settings.value.screenProtection
            DisposableEffect(screenProtection) {
                if (screenProtection) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
                onDispose { }
            }

            // Was registered in the composable body — a fresh registration on every
            // recomposition, and never unregistered. unregisterGalleryObserver() existed
            // but had no callers.
            val gallerySync = vm.settings.value.gallerySync
            DisposableEffect(gallerySync) {
                if (gallerySync) {
                    contentResolver.registerContentObserver(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        true,
                        vm.galleryObserver
                    )
                }
                onDispose {
                    if (gallerySync) unregisterGalleryObserver(this@MainActivity, vm.galleryObserver)
                }
            }

            NotesTheme(settingsViewModel!!) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    navController = rememberNavController()

                    AppNavHost(
                        settingsViewModel!!,
                        navController,
                        vm.defaultRoute!!
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settingsViewModel?.let { vm ->
            vm.cancelAutoLock()
            vm.onAppForeground()
            // Re-lock if session was cleared while in background
            if (vm.settings.value.isSetup && vm.sessionPassword == null) {
                if (::navController.isInitialized) {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        settingsViewModel?.onAppBackground()
    }

    override fun onStop() {
        super.onStop()
        // onPause already started the timer — nothing extra needed
    }

    private fun showTamperDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.tamper_dialog_title)
            .setMessage(R.string.tamper_dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.tamper_dialog_exit) { _, _ -> finishAndRemoveTask() }
            .show()
    }
}
