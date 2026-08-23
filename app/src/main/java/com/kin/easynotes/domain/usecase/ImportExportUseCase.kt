package com.kin.easynotes.domain.usecase

import android.net.Uri
import android.util.Log
import com.kin.easynotes.data.repository.ImportExportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ImportResult(
    val successful: Int,
    val total: Int,
)

class ImportExportUseCase @Inject constructor(
    private val noteUseCase: NoteUseCase,
    private val coroutineScope: CoroutineScope,
    private val fileRepository: ImportExportRepository,
)
{
    /**
     * Import notes from text files.
     * Goes through NoteUseCase so imported notes are encrypted at rest,
     * the same as notes created in the editor.
     */
    fun importNotes(uris: List<Uri>, onResult: (ImportResult) -> Unit) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            var successful = 0
            uris.forEach{uri ->
                try {
                    // addNote reports encryption/write failure via Result rather than
                    // throwing, so the count has to inspect it — a bare call here would
                    // report every import as successful even when nothing was written.
                    val result = noteUseCase.addNote(fileRepository.importFile(uri))
                    if (result.isSuccess) {
                        successful++
                    } else {
                        Log.e(
                            ImportExportUseCase::class.simpleName,
                            "Import failed for $uri",
                            result.exceptionOrNull()
                        )
                    }
                } catch (e: Exception) {
                    Log.e(ImportExportUseCase::class.simpleName, e.message, e)
                }
            }
            withContext(Dispatchers.Main) {
                onResult(ImportResult(successful, uris.size))
            }
        }
    }
}