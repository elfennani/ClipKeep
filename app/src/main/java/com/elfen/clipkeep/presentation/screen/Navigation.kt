package com.elfen.clipkeep.presentation.screen

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.elfen.clipkeep.presentation.screen.clipper.ClipperRoute
import com.elfen.clipkeep.presentation.screen.clipper.ClipperScreen
import com.elfen.clipkeep.presentation.screen.home.HomeRoute
import com.elfen.clipkeep.presentation.screen.home.HomeScreen

@Composable
fun Navigation() {
    val backstack = rememberNavBackStack(HomeRoute);
    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        ContentTransform(
            targetContentEnter =
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(),
                    initialOffset = { it / 10 }
                ) +
                        scaleIn(
                            initialScale = 0.9f,
                            animationSpec = tween()
                        ) + fadeIn(initialAlpha = 0.5f, animationSpec = tween()),
            initialContentExit = slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(),
                targetOffset = { it / 2 }
            ) + scaleOut(targetScale = 0.9f, animationSpec = tween()) + fadeOut(
            ),
        )
    };


    NavDisplay(
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = transitionSpec,
        popTransitionSpec = transitionSpec,
        predictivePopTransitionSpec = {
            transitionSpec()
        },
        backStack = backstack,
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    onNavigate = backstack::add
                )
            }
            entry<ClipperRoute> {
                ClipperScreen(
                    onNavigate = backstack::add,
                    onBack = backstack::removeLastOrNull
                )
            }
        },
    )
}