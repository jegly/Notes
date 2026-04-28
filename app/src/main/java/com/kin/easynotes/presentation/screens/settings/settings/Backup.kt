package com.kin.easynotes.presentation.screens.settings.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kin.easynotes.R
import com.kin.easynotes.presentation.screens.settings.SettingsScaffold
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import com.kin.easynotes.presentation.screens.settings.widgets.ActionType
import com.kin.easynotes.presentation.screens.settings.widgets.SettingsBox
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CloudScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    var showExportWarning by remember { mutableStateOf(false) }
    var pendingExportAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val exportTxtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            if (uri != null) settingsViewModel.onExportAllAsTxt(uri, context)
        }
    )

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris -> settingsViewModel.onImportFiles(uris, context) }
    )

    // Warning dialog shown before txt export — plaintext, user must acknowledge
    if (showExportWarning) {
        AlertDialog(
            onDismissRequest = { showExportWarning = false },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(id = R.string.export_warning_title)) },
            text = { Text(stringResource(id = R.string.export_warning_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showExportWarning = false
                    pendingExportAction?.invoke()
                    pendingExportAction = null
                }) {
                    Text(stringResource(id = R.string.export_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportWarning = false; pendingExportAction = null }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.export_import),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.export_txt_title),
                    description = stringResource(id = R.string.export_txt_description),
                    icon = Icons.Rounded.Download,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isFirst = true),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        onExit()
                        pendingExportAction = {
                            exportTxtLauncher.launch(
                                "notes-export-${currentDateTime()}.txt"
                            )
                        }
                        showExportWarning = true
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.file_import_title),
                    description = stringResource(id = R.string.file_import_description),
                    icon = Icons.Rounded.FileOpen,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        onExit()
                        importFileLauncher.launch(arrayOf("text/*"))
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

fun currentDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ms")
    return LocalDateTime.now().format(formatter)
}
