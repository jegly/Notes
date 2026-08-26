package com.kin.easynotes.domain.usecase

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kin.easynotes.data.repository.NoteRepositoryImpl
import com.kin.easynotes.domain.model.Note
import com.kin.easynotes.presentation.components.DecryptionResult
import com.kin.easynotes.presentation.components.EncryptionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val noteRepository: NoteRepositoryImpl,
    private val coroutineScope: CoroutineScope,
    private val encryptionHelper: EncryptionHelper
) {
    var notes: List<Note> by mutableStateOf(emptyList())
        private set

    // Tracks the overall decryption health across all notes
    var decryptionResult: DecryptionResult by mutableStateOf(DecryptionResult.LOADING)

    private var observeJob: Job? = null

    /**
     * Decrypted content, keyed by note id.
     *
     * Room re-emits the whole table on every write, so without this, saving one note
     * re-decrypts every other note — and each decryption is a full pass of the note body
     * through the secure element. With a large note in the database that made every write
     * anywhere in the app (including creating a brand new note) block on decrypting it.
     *
     * Ciphertext is randomized per encryption, so an unchanged ciphertext string is a
     * reliable "this note has not been rewritten" signal.
     *
     * Holds plaintext, so [zeroize] must clear it — which it can do from the main thread
     * while a decryption pass is still iterating, hence the concurrent map.
     */
    private class CacheEntry(val cipherName: String, val cipherDesc: String, val note: Note)
    private val decryptCache = ConcurrentHashMap<Int, CacheEntry>()

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

                // Decryption cost scales with note size and runs through the Keystore, so it
                // must never happen on the main thread — this scope is Main-dispatched.
                val processed = withContext(Dispatchers.Default) {
                    val result = rawNotes.mapNotNull { note ->
                        ensureActive()
                        if (!note.encrypted) {
                            // Plain note — shouldn't exist after setup but handle gracefully
                            return@mapNotNull note
                        }
                        val cached = decryptCache[note.id]
                        if (cached != null &&
                            cached.cipherName == note.name &&
                            cached.cipherDesc == note.description
                        ) {
                            // Unchanged ciphertext — reuse the plaintext, but take metadata
                            // from the fresh row.
                            return@mapNotNull cached.note.copy(
                                pinned = note.pinned,
                                createdAt = note.createdAt
                            )
                        }
                        val (decryptedNote, status) = decryptNote(note)
                        // Track worst result across all notes — don't let last note win
                        if (status != DecryptionResult.SUCCESS) {
                            worstResult = status
                            decryptCache.remove(note.id)
                            return@mapNotNull null
                        }
                        decryptCache[note.id] = CacheEntry(note.name, note.description, decryptedNote)
                        decryptedNote
                    }
                    // Drop cache entries for notes that no longer exist.
                    decryptCache.keys.retainAll(rawNotes.mapTo(HashSet()) { it.id })
                    result
                }

                decryptionResult = if (rawNotes.isEmpty()) DecryptionResult.EMPTY else worstResult
                notes = processed
            }
        }
    }

    private fun encryptNote(note: Note): Result<Note> {
        val name = encryptionHelper.encrypt(note.name).getOrElse { return Result.failure(it) }
        val description = encryptionHelper.encrypt(note.description).getOrElse { return Result.failure(it) }
        return Result.success(note.copy(name = name, description = description, encrypted = true))
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

    /**
     * Encrypt and persist [note]. Returns the note's id on success.
     *
     * Returns a failure — rather than throwing — when the Keystore key is unavailable
     * (device locked, key missing). Callers are expected to keep the plaintext and
     * retry, since the note has NOT been written in that case.
     */
    suspend fun addNote(note: Note): Result<Int> {
        val noteToSave = encryptNote(note).getOrElse { return Result.failure(it) }
        return runCatching {
            if (note.id == 0) {
                val rowId = noteRepository.addNote(noteToSave)
                if (rowId <= 0L) error("Insert was ignored by the database")
                rowId.toInt()
            } else {
                noteRepository.updateNote(noteToSave)
                note.id
            }
        }
    }

    fun pinNote(note: Note) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            addNote(note)
        }
    }

    fun deleteNoteById(id: Int) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            val noteToDelete = noteRepository.getNoteById(id).first() ?: return@launch
            noteRepository.deleteNote(noteToDelete)
        }
    }

    fun getNoteById(id: Int): Flow<Note?> = noteRepository.getNoteById(id)

    /**
     * Number of rows actually in the database, regardless of whether they decrypt.
     *
     * [notes] only contains successfully decrypted notes, so callers that export user data
     * must compare against this before writing a file — otherwise an undecryptable note is
     * silently omitted from a backup the user believes is complete. Returns -1 if unknown.
     */
    suspend fun rawNoteCount(): Int =
        runCatching { noteRepository.getAllNotes().first().size }.getOrDefault(-1)

    /**
     * Zeroize in-memory decrypted note content when the app locks.
     * Clears the notes list so decrypted text doesn't linger in Compose state.
     * The JVM GC cannot be forced, but removing references is the best we can do
     * without native code.
     */
    fun zeroize() {
        observeJob?.cancel()
        observeJob = null
        notes = emptyList()
        // The decryption cache holds plaintext — it has to go too, or locking the app
        // would leave every note readable in memory.
        decryptCache.clear()
        decryptionResult = DecryptionResult.LOADING
    }

    /**
     * Re-encrypts all notes in the database using the current password in EncryptionHelper.
     * Call this after changing the password (old password used to decrypt, new already loaded).
     * Also call on first setup to encrypt any pre-existing plain notes.
     */
    suspend fun reEncryptAllNotes(oldPassword: String?): Result<Unit> {
        val rawNotes = runCatching { noteRepository.getAllNotes().first() }
            .getOrElse { return Result.failure(it) }
        rawNotes.forEach { note ->
            val plainName: String
            val plainDesc: String
            if (note.encrypted && oldPassword != null) {
                // Decrypt with old password first
                val tempHelper = EncryptionHelper(StringBuilder(oldPassword), encryptionHelper.keystoreManager)
                val (name, _) = tempHelper.decrypt(note.name)
                val (desc, _) = tempHelper.decrypt(note.description)
                plainName = name ?: note.name
                plainDesc = desc ?: note.description
            } else {
                // Plain note — encrypt as-is
                plainName = note.name
                plainDesc = note.description
            }
            // Re-encrypt with the current Keystore key. Abort the whole pass on the first
            // failure rather than leaving the table half-converted.
            val name = encryptionHelper.encrypt(plainName).getOrElse { return Result.failure(it) }
            val desc = encryptionHelper.encrypt(plainDesc).getOrElse { return Result.failure(it) }
            runCatching {
                noteRepository.updateNote(note.copy(name = name, description = desc, encrypted = true))
            }.getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
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
