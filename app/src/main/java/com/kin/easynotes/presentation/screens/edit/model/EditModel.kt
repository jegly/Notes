package com.kin.easynotes.presentation.screens.edit.model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kin.easynotes.domain.model.Note
import com.kin.easynotes.domain.usecase.NoteUseCase
import com.kin.easynotes.presentation.components.DecryptionResult
import com.kin.easynotes.presentation.components.EncryptionHelper
import com.kin.easynotes.presentation.screens.edit.components.UndoRedoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val noteUseCase: NoteUseCase,
    private val encryption: EncryptionHelper
) : ViewModel() {
    private val _noteName = mutableStateOf(TextFieldValue())
    val noteName: State<TextFieldValue> get() = _noteName

    private val _isDescriptionInFocus = mutableStateOf(false)
    val isDescriptionInFocus: State<Boolean> get() = _isDescriptionInFocus

    private val _noteDescription = mutableStateOf(TextFieldValue())
    val noteDescription: State<TextFieldValue> get() = _noteDescription

    private val _noteId = mutableIntStateOf(0)
    val noteId: State<Int> get() = _noteId

    private val _noteCreatedTime = mutableLongStateOf(System.currentTimeMillis())
    val noteCreatedTime: State<Long> get() = _noteCreatedTime

    private val _isNoteInfoVisible = mutableStateOf(false)
    val isNoteInfoVisible: State<Boolean> get() = _isNoteInfoVisible

    private val _isEditMenuVisible = mutableStateOf(false)
    val isEditMenuVisible: State<Boolean> get() = _isEditMenuVisible

    private val _isPinned = mutableStateOf(false)
    val isPinned: State<Boolean> get() = _isPinned

    // One history per field. These used to share a single UndoRedoState, so typing in the
    // title pushed the title text onto the same stack undo() reads from — and undo would
    // then overwrite the note body with the title.
    private val nameUndoRedoState = UndoRedoState()
    private val descriptionUndoRedoState = UndoRedoState()
    private var isDeleted = false

    /**
     * Set when a save could not be completed — almost always because the Keystore key
     * requires an unlocked device and the save was attempted on a lock transition.
     * The editor keeps the plaintext in memory so the user can retry.
     */
    private val _saveError = mutableStateOf<String?>(null)
    val saveError: State<String?> get() = _saveError

    /**
     * True while an existing note is being decrypted. Decryption moved off the main thread,
     * so without this the editor paints an empty note first and fills it in later — which
     * looks exactly like the note was lost.
     */
    private val _isNoteLoading = mutableStateOf(false)
    val isNoteLoading: State<Boolean> get() = _isNoteLoading

    // Serialises saves so a debounced autosave and an explicit save can never race and
    // insert the same new note twice.
    private val saveMutex = Mutex()
    private var autoSaveJob: Job? = null

    fun clearSaveError() { _saveError.value = null }

    /**
     * Persist the current editor contents.
     *
     * Never throws: encryption fails legitimately when the device locks, and this is
     * called from lifecycle callbacks where an escaping exception would kill the process.
     */
    fun saveNote(id: Int) {
        if (isDeleted) return
        if (noteName.value.text.isEmpty() && noteDescription.value.text.isBlank()) return
        viewModelScope.launch { persist(id) }
    }

    /**
     * Save and invoke [onDone] once the write has actually landed. Used by the Save
     * action so the note is committed while the app is still foregrounded and unlocked,
     * instead of relying on a lifecycle callback that fires as the device locks.
     */
    fun saveNoteAndThen(id: Int, onDone: () -> Unit) {
        if (isDeleted || (noteName.value.text.isEmpty() && noteDescription.value.text.isBlank())) {
            onDone(); return
        }
        viewModelScope.launch {
            persist(id)
            onDone()
        }
    }

    /**
     * Fingerprint of the last content successfully written, so an autosave tick that finds
     * nothing changed can skip the write entirely.
     *
     * Encryption cost scales with note length, and every write also makes Room re-emit the
     * whole table, so a redundant save of a large note is expensive twice over. Stored as
     * hashes and lengths rather than the strings themselves to avoid retaining a second
     * copy of the note in memory.
     */
    private data class SavedSignature(
        val id: Int,
        val nameHash: Int,
        val nameLength: Int,
        val descriptionHash: Int,
        val descriptionLength: Int,
        val pinned: Boolean,
    )

    private var lastSaved: SavedSignature? = null

    /** Result of decrypting both fields of a note off the main thread. */
    private data class DecryptedNote(
        val name: String?,
        val nameStatus: DecryptionResult,
        val description: String?,
        val descriptionStatus: DecryptionResult,
    )

    private suspend fun persist(id: Int) = saveMutex.withLock {
        // Snapshot the editor state on the caller's thread (main) before handing off.
        val snapshot = Note(
            id = if (id != 0) id else noteId.value,
            name = noteName.value.text,
            description = noteDescription.value.text,
            pinned = isPinned.value,
            encrypted = true, // always encrypted
            createdAt = if (noteCreatedTime.value != 0L) noteCreatedTime.value else System.currentTimeMillis(),
        )
        val signature = SavedSignature(
            id = snapshot.id,
            nameHash = snapshot.name.hashCode(),
            nameLength = snapshot.name.length,
            descriptionHash = snapshot.description.hashCode(),
            descriptionLength = snapshot.description.length,
            pinned = snapshot.pinned,
        )
        // Nothing changed since the last successful write — don't pay for it again.
        if (signature == lastSaved) return@withLock

        // Encryption goes through the secure element and StrongBox is slow, so keep it off
        // the main thread. NonCancellable: a save started as the editor is torn down must
        // finish, or the note is silently lost.
        val result = withContext(NonCancellable + Dispatchers.IO) {
            noteUseCase.addNote(snapshot)
        }
        withContext(NonCancellable + Dispatchers.Main) {
            result.fold(
                onSuccess = { savedId ->
                    // Adopt the generated row id so subsequent saves update rather than
                    // insert a duplicate.
                    if (noteId.value == 0) updateNoteId(savedId)
                    lastSaved = signature.copy(id = savedId)
                    _saveError.value = null
                },
                onFailure = { error ->
                    // Leave the fingerprint alone so the next tick retries this content.
                    lastSaved = null
                    _saveError.value = error.message ?: "Could not save note"
                }
            )
        }
    }

    /**
     * Debounced autosave. The only previous save trigger was ON_STOP, which fires exactly
     * when the device is locking and the Keystore key becomes unusable — so a note could
     * never be written at all. Saving while the user is still typing means the foreground,
     * unlocked path does the real work.
     */
    private fun scheduleAutoSave() {
        if (isDeleted) return
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTOSAVE_DEBOUNCE_MS)
            persist(noteId.value)
        }
    }

    private companion object {
        const val AUTOSAVE_DEBOUNCE_MS = 750L
    }

    fun deleteNote(id: Int) {
        isDeleted = true
        // Cancel any in-flight autosave first, or it can re-insert the note after deletion.
        autoSaveJob?.cancel()
        noteUseCase.deleteNoteById(id = id)
    }

    private fun syncNote(note: Note) {
        // Notes come from NoteUseCase already decrypted — just display as-is
        updateNoteName(TextFieldValue(note.name, selection = TextRange(note.name.length)))
        updateNoteDescription(TextFieldValue(note.description, selection = TextRange(note.description.length)))
        updateNoteCreatedTime(note.createdAt)
        updateNoteId(note.id)
        updateNotePin(note.pinned)
        // Loading is not an edit — see the equivalent note in setupNoteData.
        lastSaved = SavedSignature(
            id = note.id,
            nameHash = note.name.hashCode(),
            nameLength = note.name.length,
            descriptionHash = note.description.hashCode(),
            descriptionLength = note.description.length,
            pinned = note.pinned,
        )
    }

    // Guards against re-loading. This used to collect the note Flow continuously, so every
    // write re-emitted the row and reset the text field — moving the caret to the end of
    // the note while the user was still typing.
    private var isLoaded = false

    fun setupNoteData(id: Int = noteId.value) {
        if (id == 0 || isLoaded) return
        isLoaded = true
        _isNoteLoading.value = true
        viewModelScope.launch {
            try {
                loadNote(id)
            } finally {
                _isNoteLoading.value = false
            }
        }
    }

    private suspend fun loadNote(id: Int) {
        val note = runCatching { noteUseCase.getNoteById(id).first() }.getOrNull() ?: return
        // Note from DB is still raw ciphertext — decrypt for display
        if (note.encrypted) {
            // viewModelScope is main-dispatched and decryption cost scales with note
            // size, so opening a large note here froze the UI for as long as the
            // secure element took to run through the whole body.
            val (name, nameStatus, description, descStatus) = withContext(Dispatchers.Default) {
                val (n, nStatus) = encryption.decrypt(note.name)
                val (d, dStatus) = encryption.decrypt(note.description)
                DecryptedNote(n, nStatus, d, dStatus)
            }
            if (nameStatus == DecryptionResult.SUCCESS && descStatus == DecryptionResult.SUCCESS) {
                updateNoteName(TextFieldValue(name!!, selection = TextRange(name.length)))
                updateNoteDescription(TextFieldValue(description!!, selection = TextRange(description.length)))
                // Opening a note is not an edit. Without this, backing straight out of a
                // note re-encrypted and rewrote it unchanged.
                lastSaved = SavedSignature(
                    id = note.id,
                    nameHash = name.hashCode(),
                    nameLength = name.length,
                    descriptionHash = description.hashCode(),
                    descriptionLength = description.length,
                    pinned = note.pinned,
                )
            } else {
                // Could not decrypt — surface it instead of silently showing an empty
                // editor that would overwrite the real note on the next save.
                isDeleted = true
                _saveError.value = "This note could not be decrypted on this device."
            }
        } else {
            syncNote(note)
        }
        updateNoteCreatedTime(note.createdAt)
        updateNoteId(note.id)
        updateNotePin(note.pinned)
        // Loading the note counts as input to the undo stack; don't let it trigger a write.
        autoSaveJob?.cancel()
    }

    fun toggleEditMenuVisibility(value: Boolean) { _isEditMenuVisible.value = value }
    fun toggleNoteInfoVisibility(value: Boolean) { _isNoteInfoVisible.value = value }
    fun toggleIsDescriptionInFocus(value: Boolean) { _isDescriptionInFocus.value = value }
    fun toggleNotePin(value: Boolean) { _isPinned.value = value }

    fun updateNoteName(newName: TextFieldValue) {
        _noteName.value = newName
        nameUndoRedoState.onInput(newName)
        scheduleAutoSave()
    }

    fun updateNoteDescription(newDescription: TextFieldValue) {
        _noteDescription.value = newDescription
        descriptionUndoRedoState.onInput(newDescription)
        scheduleAutoSave()
    }

    private fun updateNoteCreatedTime(newTime: Long) { _noteCreatedTime.longValue = newTime }
    private fun updateNotePin(pinned: Boolean) { _isPinned.value = pinned }
    fun updateNoteId(newId: Int) { _noteId.intValue = newId }

    // The undo/redo affordances are only shown while the description has focus, so both
    // act on the description history.
    fun undo() { descriptionUndoRedoState.undo(); _noteDescription.value = descriptionUndoRedoState.input }
    fun redo() { descriptionUndoRedoState.redo(); _noteDescription.value = descriptionUndoRedoState.input }

    private fun isSelectorAtStartOfNonEmptyLine(): Boolean {
        val text = _noteDescription.value.text
        val selectionStart = _noteDescription.value.selection.start
        if (selectionStart == 0) return true
        return text[selectionStart - 1] == '\n'
    }

    private fun getIntRangeForCurrentLine(): IntRange {
        val text = _noteDescription.value.text
        val selectionStart = _noteDescription.value.selection.start
        val selectionEnd = _noteDescription.value.selection.end
        var lineStart = selectionStart
        var lineEnd = selectionEnd
        while (lineStart > 0 && text[lineStart - 1] != '\n') lineStart--
        while (lineEnd < text.length && text[lineEnd] != '\n') lineEnd++
        return IntRange(lineStart, lineEnd - 1)
    }

    fun insertText(insertText: String, offset: Int = 1, newLine: Boolean = true) {
        val currentText = _noteDescription.value.text
        val resultSelectionIndex: Int
        val rangeOfCurrentLine = getIntRangeForCurrentLine()
        val updatedText = if (!rangeOfCurrentLine.isEmpty()) {
            val currentLineContents = currentText.substring(rangeOfCurrentLine)
            val newLineText = if (isSelectorAtStartOfNonEmptyLine()) {
                insertText + currentLineContents
            } else {
                if (newLine) currentLineContents + "\n" + insertText
                else currentLineContents + insertText
            }
            resultSelectionIndex = rangeOfCurrentLine.first + newLineText.length - 1
            currentText.replaceRange(rangeOfCurrentLine, newLineText)
        } else {
            resultSelectionIndex = (currentText + insertText).length
            currentText + insertText
        }
        _noteDescription.value = TextFieldValue(
            text = updatedText,
            selection = TextRange(resultSelectionIndex + offset)
        )
    }
}
