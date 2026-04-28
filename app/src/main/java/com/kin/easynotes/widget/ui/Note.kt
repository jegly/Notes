package com.kin.easynotes.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kin.easynotes.domain.model.Note
import com.kin.easynotes.domain.usecase.NoteUseCase
import com.kin.easynotes.presentation.MainActivity
import com.kin.easynotes.presentation.components.markdown.WidgetText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SelectedNote(note: Note, noteUseCase: NoteUseCase, widgetId: Int) {
    val context = LocalContext.current
    val glanceModifier = GlanceModifier.clickable {
        // Security: open MainActivity with the noteId; MainActivity will gate
        // behind the lock screen if the app is currently locked before revealing
        // any note content.
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("noteId", note.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }

    Column(
        modifier = glanceModifier
            .background(GlanceTheme.colors.background)
            .fillMaxSize()
            .padding(6.dp)
    ) {
        if (note.name.isNotBlank()) {
            WidgetText(
                modifier = glanceModifier,
                markdown = note.name,
                weight = FontWeight.Bold,
                fontSize = 24.sp,
                color = GlanceTheme.colors.primary,
                onContentChange = {
                    CoroutineScope(Dispatchers.IO).launch {
                        noteUseCase.addNote(note.copy(name = it))
                        noteUseCase.observe()
                    }
                }
            )
        }
        if (note.description.isNotBlank()) {
            WidgetText(
                modifier = glanceModifier.fillMaxWidth(),
                markdown = note.description,
                weight = FontWeight.Normal,
                fontSize = 12.sp,
                color = GlanceTheme.colors.primary,
                onContentChange = {
                    CoroutineScope(Dispatchers.IO).launch {
                        noteUseCase.addNote(note.copy(description = it))
                        noteUseCase.observe()
                    }
                }
            )
        }
    }
}

/** Shown in place of note content when the app lock is enabled and the device is locked. */
@Composable
fun LockedNoteWidget(widgetId: Int) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .background(GlanceTheme.colors.background)
            .fillMaxSize()
            .padding(8.dp)
            .clickable {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\uD83D\uDD12",   // 🔒
            style = TextStyle(fontSize = 28.sp)
        )
        Text(
            text = "Tap to unlock",
            style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onBackground)
        )
    }
}
