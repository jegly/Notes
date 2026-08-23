package com.kin.easynotes.presentation.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val SLIDE_FRACTION_DEN = 20

private fun fractionOffset(full: Int): Int = full / SLIDE_FRACTION_DEN

fun getNoteEnterAnimation(): EnterTransition =
    fadeIn(animationSpec = tween(M3Motion.DurationMedium2, easing = M3Motion.Standard)) +
        scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(M3Motion.DurationMedium4, easing = M3Motion.EmphasizedDecelerate),
        )

fun getNoteExitAnimation(slideDirection: Int): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { slideDirection * it },
        animationSpec = tween(M3Motion.DurationMedium4, easing = M3Motion.EmphasizedAccelerate),
    ) + fadeOut(animationSpec = tween(M3Motion.DurationShort4, easing = M3Motion.Standard))

// ── Shared axis X (forward push) ─────────────────────────────────────────────
// Outgoing slides slightly left and fades; incoming slides in from the right.

fun defaultScreenEnterAnimation(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fractionOffset(it) },
        animationSpec = tween(M3Motion.DurationMedium4, easing = M3Motion.EmphasizedDecelerate),
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = M3Motion.DurationMedium2,
            delayMillis = M3Motion.DurationShort2,
            easing = M3Motion.Standard,
        ),
    )

fun defaultScreenExitAnimation(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { -fractionOffset(it) },
        animationSpec = tween(M3Motion.DurationMedium4, easing = M3Motion.EmphasizedAccelerate),
    ) + fadeOut(
        animationSpec = tween(M3Motion.DurationShort3, easing = M3Motion.StandardAccelerate),
    )

// ── Shared axis X (backward pop) ─────────────────────────────────────────────
// Mirrors the forward direction: incoming from the LEFT, outgoing to the RIGHT.

fun defaultScreenPopEnterAnimation(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { -fractionOffset(it) },
        animationSpec = tween(M3Motion.DurationMedium4, easing = M3Motion.EmphasizedDecelerate),
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = M3Motion.DurationMedium2,
            delayMillis = M3Motion.DurationShort2,
            easing = M3Motion.Standard,
        ),
    )

fun defaultScreenPopExitAnimation(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fractionOffset(it) },
        animationSpec = tween(M3Motion.DurationMedium4, easing = M3Motion.EmphasizedAccelerate),
    ) + fadeOut(
        animationSpec = tween(M3Motion.DurationShort3, easing = M3Motion.StandardAccelerate),
    )

// ── Full slide (used for opening Settings — feels like a drawer push) ────────

fun slideScreenEnterAnimation(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(M3Motion.DurationMedium4, easing = M3Motion.EmphasizedDecelerate),
    )

fun slideScreenExitAnimation(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(M3Motion.DurationMedium4, easing = M3Motion.EmphasizedAccelerate),
    )

// ── Fade-through (M3 spec for identity changes — e.g., login → home) ─────────
// Outgoing fades quickly; incoming fades in after a short delay with a subtle scale.

fun fadeThroughEnterAnimation(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = 210,
            delayMillis = 90,
            easing = M3Motion.Standard,
        ),
    ) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(
            durationMillis = 210,
            delayMillis = 90,
            easing = M3Motion.Standard,
        ),
    )

fun fadeThroughExitAnimation(): ExitTransition =
    fadeOut(animationSpec = tween(durationMillis = 90, easing = M3Motion.Standard)) +
        scaleOut(
            targetScale = 0.92f,
            animationSpec = tween(durationMillis = 90, easing = M3Motion.Standard),
        )
