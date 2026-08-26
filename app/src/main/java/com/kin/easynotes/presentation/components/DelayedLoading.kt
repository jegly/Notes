package com.kin.easynotes.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * True only once [loading] has been continuously true for [delayMs].
 *
 * Decryption is normally fast enough that a progress indicator would appear and disappear
 * within a couple of frames, which reads as a flicker rather than as feedback. Holding off
 * before admitting to being busy means the indicator shows up only when there is genuinely
 * something to wait for — a large note, or a cold start with one in the database.
 */
@Composable
fun rememberDelayedLoading(loading: Boolean, delayMs: Long = DEFAULT_LOADING_DELAY_MS): Boolean {
    var elapsed by remember { mutableStateOf(false) }
    LaunchedEffect(loading) {
        if (!loading) {
            elapsed = false
            return@LaunchedEffect
        }
        delay(delayMs)
        elapsed = true
    }
    // Guard on `loading` too, for the frame between it clearing and the effect restarting.
    return elapsed && loading
}

/** Long enough that ordinary loads never flash, short enough to feel responsive. */
const val DEFAULT_LOADING_DELAY_MS = 250L
