package com.kin.easynotes.presentation.screens.settings.settings

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.kin.easynotes.R
import com.kin.easynotes.core.constant.DatabaseConst
import com.kin.easynotes.presentation.screens.settings.SettingsScaffold
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import com.kin.easynotes.presentation.screens.settings.widgets.ActionType
import com.kin.easynotes.presentation.screens.settings.widgets.SettingsBox
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CloudScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    var showRestorePasswordPrompt by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/.zip"),
        onResult = { uri ->
            // Always encrypt export using session password
            if (uri != null) settingsViewModel.onExportBackup(uri, context)
        }
    )

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                pendingRestoreUri = uri
                showRestorePasswordPrompt = true
            }
        }
    )

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris -> settingsViewModel.onImportFiles(uris, context) }
    )

    // Restore password prompt — user enters the password used when this backup was made
    if (showRestorePasswordPrompt && pendingRestoreUri != null) {
        RestorePasswordDialog(
            context = context,
            onConfirm = { password ->
                showRestorePasswordPrompt = false
                settingsViewModel.onImportBackupWithPassword(pendingRestoreUri!!, password, context)
                pendingRestoreUri = null
            },
            onDismiss = {
                showRestorePasswordPrompt = false
                pendingRestoreUri = null
            }
        )
    }

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.backup),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.backup),
                    description = stringResource(id = R.string.backup_description),
                    icon = Icons.Rounded.Backup,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isFirst = true),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        LaunchedEffect(true) {
                            exportBackupLauncher.launch(
                                "${DatabaseConst.NOTES_DATABASE_BACKUP_NAME}-${currentDateTime()}.zip"
                            )
                        }
                        onExit()
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.restore),
                    description = stringResource(id = R.string.restore_description),
                    icon = Icons.Rounded.ImportExport,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        LaunchedEffect(true) {
                            importBackupLauncher.launch(arrayOf("application/zip"))
                        }
                        onExit()
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = context.getString(R.string.file_import_title),
                    description = context.getString(R.string.file_import_description),
                    icon = Icons.Rounded.FileOpen,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isBoth = true),
                    actionType = ActionType.CUSTOM,
                    customAction = {
                        LaunchedEffect(true) {
                            importFileLauncher.launch(arrayOf("text/*"))
                        }
                    }
                )
            }
        }
    }
}

/**
 * Dialog shown before restore — user enters the password that was used
 * when the backup was originally exported. This may differ from the
 * current session password (e.g. restoring from another device).
 */
@Composable
private fun RestorePasswordDialog(
    context: Context,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.restore_password_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.restore_password_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.enter_password)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (password.isNotBlank()) onConfirm(password)
                    else Toast.makeText(context, R.string.invalid_input, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.End)
            ) {
                Text(stringResource(id = R.string.restore))
            }
        }
    }
}

fun currentDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ms")
    return LocalDateTime.now().format(formatter)
}
