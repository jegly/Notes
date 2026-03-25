package com.kin.easynotes.domain.usecase

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import com.kin.easynotes.data.repository.NoteRepositoryImpl
import com.kin.easynotes.domain.model.Note
import com.kin.easynotes.presentation.components.DecryptionResult
import com.kin.easynotes.presentation.components.EncryptionHelper
import com.kin.easynotes.widget.NotesWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val noteRepository: NoteRepositoryImpl,
    private val coroutineScope: CoroutineScope,
    private val encryptionHelper: EncryptionHelper,
    @ApplicationContext private val context: Context
) {
    var notes: List<Note> by mutableStateOf(emptyList())
        private set

    // Tracks the overall decryption health across all notes
    var decryptionResult: DecryptionResult by mutableStateOf(DecryptionResult.LOADING)

    private var observeJob: Job? = null

    fun observe() {
        observeJob?.cancel()
        observeJob = coroutineScope.launch {
            noteRepository.getAllNotes().collectLatest { rawNotes ->
                if (encryptionHelper.isPasswordEmpty()) {
                    // Password not loaded yet — show nothing, wait for login
                    decryptionResult = DecryptionResult.LOADING
                    notes = emptyList()
                    return@collectLatest
                }

                var worstResult: DecryptionResult = DecryptionResult.SUCCESS
                val processed = rawNotes.mapNotNull { note ->
                    if (note.encrypted) {
                        val (decryptedNote, status) = decryptNote(note)
                        // Track worst result across all notes — don't let last note win
                        if (status != DecryptionResult.SUCCESS) {
                            worstResult = status
                        }
                        if (status == DecryptionResult.SUCCESS) decryptedNote else null
                    } else {
                        // Plain note — shouldn't exist after setup but handle gracefully
                        note
                    }
                }

                decryptionResult = if (rawNotes.isEmpty()) DecryptionResult.EMPTY else worstResult
                notes = processed
                NotesWidget().updateAll(context)
            }
        }
    }

    private fun encryptNote(note: Note): Note {
        return note.copy(
            name = encryptionHelper.encrypt(note.name),
            description = encryptionHelper.encrypt(note.description),
            encrypted = true
        )
    }

    private fun decryptNote(note: Note): Pair<Note, DecryptionResult> {
        val (decryptedName, nameResult) = encryptionHelper.decrypt(note.name)
        val (decryptedDescription, descResult) = encryptionHelper.decrypt(note.description)
        // Use the worse of the two results
        val result = if (nameResult == DecryptionResult.SUCCESS && descResult == DecryptionResult.SUCCESS)
            DecryptionResult.SUCCESS else descResult
        return Pair(
            note.copy(
                name = decryptedName ?: "",
                description = decryptedDescription ?: ""
            ),
            result
        )
    }

    suspend fun addNote(note: Note) {
        // Always encrypt every note
        val noteToSave = encryptNote(note)
        if (note.id == 0) {
            noteRepository.addNote(noteToSave)
        } else {
            noteRepository.updateNote(noteToSave)
        }
    }

    fun pinNote(note: Note) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            addNote(note)
        }
    }

    fun deleteNoteById(id: Int) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            val noteToDelete = noteRepository.getNoteById(id).first()
            noteRepository.deleteNote(noteToDelete)
        }
    }

    fun getNoteById(id: Int): Flow<Note> = noteRepository.getNoteById(id)

    fun getLastNoteId(onResult: (Long?) -> Unit) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            val lastNoteId = noteRepository.getLastNoteId()
            withContext(Dispatchers.Main) { onResult(lastNoteId) }
        }
    }

    /**
     * Re-encrypts all notes in the database using the current password in EncryptionHelper.
     * Call this after changing the password (old password used to decrypt, new already loaded).
     * Also call on first setup to encrypt any pre-existing plain notes.
     */
    suspend fun reEncryptAllNotes(oldPassword: String?) {
        val rawNotes = noteRepository.getAllNotes().first()
        rawNotes.forEach { note ->
            val plainName: String
            val plainDesc: String
            if (note.encrypted && oldPassword != null) {
                // Decrypt with old password first
                val tempHelper = EncryptionHelper(StringBuilder(oldPassword))
                val (name, _) = tempHelper.decrypt(note.name)
                val (desc, _) = tempHelper.decrypt(note.description)
                plainName = name ?: note.name
                plainDesc = desc ?: note.description
            } else {
                // Plain note — encrypt as-is
                plainName = note.name
                plainDesc = note.description
            }
            // Re-encrypt with current password (already loaded in encryptionHelper)
            val reEncrypted = note.copy(
                name = encryptionHelper.encrypt(plainName),
                description = encryptionHelper.encrypt(plainDesc),
                encrypted = true
            )
            noteRepository.updateNote(reEncrypted)
        }
    }

    /**
     * Decrypts all notes back to plaintext — call when password is being removed.
     */
    suspend fun decryptAllNotes() {
        val rawNotes = noteRepository.getAllNotes().first()
        rawNotes.forEach { note ->
            if (note.encrypted) {
                val (name, _) = encryptionHelper.decrypt(note.name)
                val (desc, _) = encryptionHelper.decrypt(note.description)
                noteRepository.updateNote(
                    note.copy(
                        name = name ?: note.name,
                        description = desc ?: note.description,
                        encrypted = false
                    )
                )
            }
        }
    }
}
