package com.kin.easynotes.presentation.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kin.easynotes.presentation.components.defaultScreenEnterAnimation
import com.kin.easynotes.presentation.components.defaultScreenExitAnimation
import com.kin.easynotes.presentation.components.defaultScreenPopEnterAnimation
import com.kin.easynotes.presentation.components.defaultScreenPopExitAnimation
import com.kin.easynotes.presentation.components.fadeThroughEnterAnimation
import com.kin.easynotes.presentation.components.fadeThroughExitAnimation
import com.kin.easynotes.presentation.components.slideScreenEnterAnimation
import com.kin.easynotes.presentation.components.slideScreenExitAnimation

/**
 * Default screen transition — Material 3 Shared Axis X.
 * Forward push slides the new screen in from the right; back pop reverses it.
 */
fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { defaultScreenEnterAnimation() },
    exitTransition = { defaultScreenExitAnimation() },
    popEnterTransition = { defaultScreenPopEnterAnimation() },
    popExitTransition = { defaultScreenPopExitAnimation() },
    content = content
)

/**
 * Full-width slide — used for top-level destinations like Settings.
 * The previous screen stays put while the new one slides in over it.
 */
fun NavGraphBuilder.slideInComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { slideScreenEnterAnimation() },
    exitTransition = { defaultScreenExitAnimation() },
    popEnterTransition = { defaultScreenPopEnterAnimation() },
    popExitTransition = { slideScreenExitAnimation() },
    content = content
)

/**
 * Fade-through — Material 3 spec for identity changes (login → home, setup → home).
 * Old content fades out, new content fades in with a subtle scale.
 */
fun NavGraphBuilder.fadeThroughComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { fadeThroughEnterAnimation() },
    exitTransition = { fadeThroughExitAnimation() },
    popEnterTransition = { fadeThroughEnterAnimation() },
    popExitTransition = { fadeThroughExitAnimation() },
    content = content
)
