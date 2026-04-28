package com.kin.easynotes.presentation.screens.home.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.kin.easynotes.domain.model.Note
import com.kin.easynotes.domain.usecase.NoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val noteUseCase: NoteUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    var selectedNotes = mutableStateListOf<Note>()

    private var _isDeleteMode = mutableStateOf(false)
    val isDeleteMode: State<Boolean> = _isDeleteMode

    private var _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    init { noteUseCase.observe() }

    fun toggleIsDeleteMode(enabled: Boolean) { _isDeleteMode.value = enabled }

    fun changeSearchQuery(newValue: String) { _searchQuery.value = newValue }

    fun pinOrUnpinNotes() {
        if (selectedNotes.all { it.pinned }) {
            selectedNotes.forEach { noteUseCase.pinNote(it.copy(pinned = false)) }
        } else {
            selectedNotes.forEach { noteUseCase.pinNote(it.copy(pinned = true)) }
        }
        selectedNotes.clear()
    }

    fun getAllNotes(): List<Note> = noteUseCase.notes
}
