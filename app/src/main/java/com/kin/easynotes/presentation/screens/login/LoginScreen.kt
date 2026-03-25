package com.kin.easynotes.presentation.screens.login

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kin.easynotes.R
import com.kin.easynotes.presentation.navigation.NavRoutes
import com.kin.easynotes.presentation.popUpToTop
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel

@Composable
fun LoginScreen(
    settingsViewModel: SettingsViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    BackHandler { (context as? ComponentActivity)?.finish() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    fun attempt() {
        if (settingsViewModel.verifyPassword(password)) {
            settingsViewModel.onLoginSuccess(password)
            val pending = settingsViewModel.pendingWidgetNoteId
            if (pending != -1) {
                settingsViewModel.pendingWidgetNoteId = -1
                navController.navigate(NavRoutes.Edit.createRoute(pending, true)) { popUpToTop(navController) }
            } else {
                navController.navigate(NavRoutes.Home.route) { popUpToTop(navController) }
            }
        } else {
            errorMessage = context.getString(R.string.passcode_incorrect)
            password = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.enter_password),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text(stringResource(id = R.string.enter_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { attempt() }),
            singleLine = true,
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { attempt() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(stringResource(id = R.string.enter_password))
        }
    }
}
