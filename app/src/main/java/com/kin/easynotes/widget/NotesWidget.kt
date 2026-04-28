package com.kin.easynotes.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import com.kin.easynotes.data.repository.SettingsRepositoryImpl
import com.kin.easynotes.domain.usecase.NoteUseCase
import com.kin.easynotes.widget.ui.LockedNoteWidget
import com.kin.easynotes.widget.ui.SelectedNote
import com.kin.easynotes.widget.ui.ZeroState
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetModelRepositoryEntryPoint {
    fun noteUseCase(): NoteUseCase
    fun settingsRepository(): SettingsRepositoryImpl
}

class NotesWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<List<Pair<Int, Int>>>
        get() = object : GlanceStateDefinition<List<Pair<Int, Int>>> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): DataStore<List<Pair<Int, Int>>> = NotesDataStore(context)

            override fun getLocation(context: Context, fileKey: String): File =
                throw NotImplementedError("Not implemented")
        }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ep          = getEntryPoint(context)
        val noteUseCase = ep.noteUseCase()
        val settings    = ep.settingsRepository()
        val widgetId    = GlanceAppWidgetManager(context).getAppWidgetId(id)

        // Call the suspend function here in the coroutine body, not inside provideContent
        val isLocked = settings.isAppLocked()

        provideContent {
            GlanceTheme {
                val prefs  = currentState<List<Pair<Int, Int>>>()
                val noteId = prefs.firstOrNull { it.first == widgetId }?.second

                noteUseCase.observe()

                when {
                    isLocked -> LockedNoteWidget(widgetId = widgetId)
                    noteId == null -> ZeroState(widgetId = widgetId)
                    else -> {
                        val selectedNote = noteUseCase.notes.filter { it.id == noteId }
                        when {
                            selectedNote.isEmpty() -> ZeroState(widgetId = widgetId)
                            else -> SelectedNote(selectedNote.first(), noteUseCase, widgetId = widgetId)
                        }
                    }
                }
            }
        }
    }
}

fun getEntryPoint(applicationContext: Context): WidgetModelRepositoryEntryPoint =
    EntryPoints.get(applicationContext, WidgetModelRepositoryEntryPoint::class.java)

// Legacy helper kept for call-sites that only need NoteUseCase
fun getNoteUseCase(applicationContext: Context): NoteUseCase =
    getEntryPoint(applicationContext).noteUseCase()
