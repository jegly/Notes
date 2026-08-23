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

    private val undoRedoState = UndoRedoState()
    private var isDeleted = false

    /**
     * Set when a save could not be completed — almost always because the Keystore key
     * requires an unlocked device and the save was attempted on a lock transition.
     * The editor keeps the plaintext in memory so the user can retry.
     */
    private val _saveError = mutableStateOf<String?>(null)
    val saveError: State<String?> get() = _saveError

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
                    _saveError.value = null
                },
                onFailure = { error -> _saveError.value = error.message ?: "Could not save note" }
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
    }

    // Guards against re-loading. This used to collect the note Flow continuously, so every
    // write re-emitted the row and reset the text field — moving the caret to the end of
    // the note while the user was still typing.
    private var isLoaded = false

    fun setupNoteData(id: Int = noteId.value) {
        if (id == 0 || isLoaded) return
        isLoaded = true
        viewModelScope.launch {
            val note = runCatching { noteUseCase.getNoteById(id).first() }.getOrNull() ?: return@launch
            // Note from DB is still raw ciphertext — decrypt for display
            if (note.encrypted) {
                val (name, nameStatus) = encryption.decrypt(note.name)
                val (description, descStatus) = encryption.decrypt(note.description)
                if (nameStatus == DecryptionResult.SUCCESS && descStatus == DecryptionResult.SUCCESS) {
                    updateNoteName(TextFieldValue(name!!, selection = TextRange(name.length)))
                    updateNoteDescription(TextFieldValue(description!!, selection = TextRange(description.length)))
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
    }

    fun toggleEditMenuVisibility(value: Boolean) { _isEditMenuVisible.value = value }
    fun toggleNoteInfoVisibility(value: Boolean) { _isNoteInfoVisible.value = value }
    fun toggleIsDescriptionInFocus(value: Boolean) { _isDescriptionInFocus.value = value }
    fun toggleNotePin(value: Boolean) { _isPinned.value = value }

    fun updateNoteName(newName: TextFieldValue) {
        _noteName.value = newName
        undoRedoState.onInput(newName)
        scheduleAutoSave()
    }

    fun updateNoteDescription(newDescription: TextFieldValue) {
        _noteDescription.value = newDescription
        undoRedoState.onInput(newDescription)
        scheduleAutoSave()
    }

    private fun updateNoteCreatedTime(newTime: Long) { _noteCreatedTime.longValue = newTime }
    private fun updateNotePin(pinned: Boolean) { _isPinned.value = pinned }
    fun updateNoteId(newId: Int) { _noteId.intValue = newId }

    fun undo() { undoRedoState.undo(); _noteDescription.value = undoRedoState.input }
    fun redo() { undoRedoState.redo(); _noteDescription.value = undoRedoState.input }

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
