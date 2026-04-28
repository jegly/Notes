package com.kin.easynotes.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import com.kin.easynotes.domain.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import java.io.IOException
import javax.inject.Inject

/**
 * Handles importing plain text files as notes.
 * Backup export/import has been removed — encryption keys are device-bound
 * (Android Keystore) and backups cannot be restored to a different device or
 * after reinstall. Use Export as Text for data migration.
 */
class ImportExportRepository(
    private val context: Context,
    private val mutex: Mutex,
    private val scope: CoroutineScope,
    private val dispatcher: ExecutorCoroutineDispatcher,
) {
    fun importFile(uri: Uri): Note {
        val content = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.reader().buffered().readText()
        } ?: throw IOException("Could not open file for import.")

        val name = context.contentResolver.query(
            uri,
            arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME),
            null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getStringOrNull(0) else ""
        } ?: ""

        return Note(name = name, description = content)
    }
}
