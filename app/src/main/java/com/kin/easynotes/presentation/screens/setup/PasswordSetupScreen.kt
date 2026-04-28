package com.kin.easynotes.presentation.screens.setup

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kin.easynotes.R
import com.kin.easynotes.presentation.navigation.NavRoutes
import com.kin.easynotes.presentation.popUpToTop
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel

@Composable
fun PasswordSetupScreen(
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    isChangingPassword: Boolean = false
) {
    val context = LocalContext.current
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val isDeviceSecure = settingsViewModel.keystoreManager.isDeviceSecure()

    val isEncrypting = settingsViewModel.isReEncrypting.value
    LaunchedEffect(isEncrypting) {
        if (submitted && !isEncrypting) {
            if (isChangingPassword) navController.navigateUp()
            else navController.navigate(NavRoutes.Home.route) { popUpToTop(navController) }
        }
    }

    BackHandler {
        if (!isChangingPassword) (context as? ComponentActivity)?.finish()
        else navController.navigateUp()
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Hard block if no screen lock — can't generate Keystore key without it
    if (!isDeviceSecure) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.error_no_screen_lock),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    fun attempt() {
        errorMessage = null
        if (isChangingPassword && currentPassword.isBlank()) {
            errorMessage = context.getString(R.string.enter_password); return
        }
        if (newPassword.length < 4) {
            errorMessage = context.getString(R.string.passcode_too_short); return
        }
        if (newPassword != confirmPassword) {
            errorMessage = context.getString(R.string.passcode_mismatch)
            newPassword = ""; confirmPassword = ""; return
        }
        submitted = true
        if (isChangingPassword) {
            settingsViewModel.changePassword(currentPassword, newPassword, context) { success ->
                if (!success) { submitted = false; errorMessage = context.getString(R.string.passcode_incorrect); currentPassword = "" }
            }
        } else {
            settingsViewModel.setPasswordFirstTime(newPassword, context) {}
        }
    }

    if (settingsViewModel.isReEncrypting.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(id = R.string.encrypting_notes), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Rounded.Security, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isChangingPassword) stringResource(id = R.string.change_password)
                   else stringResource(id = R.string.setup_password),
            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isChangingPassword) stringResource(id = R.string.change_password_description)
                   else stringResource(id = R.string.setup_password_description),
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (isChangingPassword) {
            OutlinedTextField(
                value = currentPassword, onValueChange = { currentPassword = it; errorMessage = null },
                label = { Text(stringResource(id = R.string.current_password)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                singleLine = true, isError = errorMessage != null,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = newPassword, onValueChange = { newPassword = it; errorMessage = null },
            label = { Text(stringResource(id = R.string.enter_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            singleLine = true, isError = errorMessage != null,
            modifier = if (!isChangingPassword) Modifier.fillMaxWidth().focusRequester(focusRequester)
                       else Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it; errorMessage = null },
            label = { Text(stringResource(id = R.string.confirm_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { attempt() }),
            singleLine = true, isError = errorMessage != null, modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { attempt() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(if (isChangingPassword) stringResource(id = R.string.change_password)
                 else stringResource(id = R.string.setup_password))
        }
    }
}
