package com.kin.easynotes.presentation.screens.edit.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    singleLine: Boolean = false,
    modifier: Modifier = Modifier,
    hideContent: Boolean = false,
    useMonoSpaceFont: Boolean = false,
) {
    val visualTransformation = if (hideContent) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    TextField(
        value = value,
        textStyle = if (useMonoSpaceFont) LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace) else LocalTextStyle.current,
        visualTransformation = visualTransformation,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            autoCorrectEnabled = !hideContent,
            keyboardType = if (hideContent) KeyboardType.Password else KeyboardType.Text,
            capitalization = if (hideContent) KeyboardCapitalization.None else KeyboardCapitalization.Sentences,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        placeholder = {
            Text(placeholder)
        }
    )
}

/**
 * Undo/redo history for a single text field.
 *
 * Every entry holds a complete copy of the field's text, so the history is bounded by both
 * a depth limit and a total-character budget. It used to be unbounded: each keystroke
 * pushed another full copy of the note and nothing was ever dropped, so editing a large
 * note grew the heap by the note's size per character typed until the process was killed.
 *
 * When a note is large enough that even a couple of snapshots blow the budget, the history
 * collapses to the current state and undo becomes a no-op. Losing undo depth on a huge note
 * is the intended trade.
 */
class UndoRedoState {
    var input by mutableStateOf(TextFieldValue(""))

    private val undoHistory = ArrayDeque<TextFieldValue>()
    private val redoHistory = ArrayDeque<TextFieldValue>()
    private var undoChars = 0L
    private var redoChars = 0L

    init {
        undoHistory.addLast(input)
    }

    fun onInput(value: TextFieldValue) {
        // Caret moves and focus changes are not edits. Recording them wasted a full copy of
        // the text per event and made undo appear to do nothing.
        if (value.text == input.text) return

        // always set the cursor at the end (selection = text length)
        val updatedValue = value.copy(value.text, selection = TextRange(value.text.length))
        undoHistory.addLast(updatedValue)
        undoChars = trim(undoHistory, undoChars + updatedValue.text.length, keepAtLeast = 1)

        redoHistory.clear()  // Clear redo history on new input
        redoChars = 0L

        input = updatedValue
    }

    fun undo() {
        if (undoHistory.size <= 1) return
        val lastState = undoHistory.removeLast()
        undoChars -= lastState.text.length
        redoHistory.addLast(lastState)
        redoChars = trim(redoHistory, redoChars + lastState.text.length, keepAtLeast = 0)
        input = undoHistory.last()
    }

    fun redo() {
        val redoState = redoHistory.removeLastOrNull() ?: return
        redoChars -= redoState.text.length
        undoHistory.addLast(redoState)
        undoChars = trim(undoHistory, undoChars + redoState.text.length, keepAtLeast = 1)
        input = redoState
    }

    /**
     * Drops the oldest entries until [history] fits both budgets. Takes the prospective
     * character total and returns the actual one.
     */
    private fun trim(history: ArrayDeque<TextFieldValue>, chars: Long, keepAtLeast: Int): Long {
        var total = chars
        while (history.size > keepAtLeast &&
            (history.size > MAX_HISTORY_DEPTH || total > MAX_HISTORY_CHARS)
        ) {
            total -= history.removeFirst().text.length
        }
        return total
    }

    private companion object {
        /** Maximum snapshots retained per field. */
        const val MAX_HISTORY_DEPTH = 50

        /** Upper bound on text retained across a history, in characters (~2 MB of UTF-16). */
        const val MAX_HISTORY_CHARS = 1_000_000L
    }
}
