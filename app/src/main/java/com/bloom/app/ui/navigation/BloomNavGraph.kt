package com.bloom.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bloom.app.ui.garden.GardenScreen
import com.bloom.app.ui.home.HomeScreen
import com.bloom.app.ui.insights.InsightsScreen
import com.bloom.app.ui.journal.JournalEditorScreen
import com.bloom.app.ui.journal.JournalListScreen
import com.bloom.app.ui.mood.MoodCheckInScreen
import com.bloom.app.ui.onboarding.OnboardingScreen
import com.bloom.app.ui.settings.SettingsScreen
import com.bloom.app.ui.splash.SplashScreen

// ─────────────────────────────────────────────────────────────────────────────
// BloomNavGraph
//
// Defines all navigation routes and their transition animations.
//
// Transition philosophy:
//   - Bottom nav switching: Fade — calm, no directional confusion
//   - Deeper navigation (editor): Slide up — feels like opening a page
//   - Back navigation: Slide down — natural return gesture
//   - Onboarding: Fade — first impression should be soft
//
// All transitions use 300ms — fast enough to feel snappy,
// slow enough to register as intentional motion.
// ─────────────────────────────────────────────────────────────────────────────

private const val TRANSITION_DURATION = 300

@Composable
fun BloomNavGraph(
    navController       : NavHostController,
    startDestination    : String,
    nextDestination     : String,
    modifier            : Modifier = Modifier,
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier,
        // Default transitions for bottom-nav switching (subtle fade)
        enterTransition  = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
        exitTransition   = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) },
        popEnterTransition  = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
        popExitTransition   = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) },
    ) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(nextDestination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding ────────────────────────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onMoodCheckIn = { mood -> navController.navigate(Screen.MoodCheckIn.createRoute(mood)) },
                onNewEntry    = { navController.navigate(Screen.JournalEditor.createRoute()) },
                onContinueEntry = { id ->
                    navController.navigate(Screen.JournalEditor.createRoute(id))
                },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // ── Journal List ──────────────────────────────────────────────────────
        composable(Screen.Journal.route) {
            JournalListScreen(
                onNewEntry  = { navController.navigate(Screen.JournalEditor.createRoute()) },
                onEntryTap  = { id -> navController.navigate(Screen.JournalEditor.createRoute(id)) },
            )
        }

        // ── Journal Editor ────────────────────────────────────────────────────
        // Slides up from the bottom — opening a journal page feeling
        composable(
            route     = Screen.JournalEditor.route,
            arguments = listOf(
                navArgument(Screen.JournalEditor.ARG_ENTRY_ID) {
                    type         = NavType.LongType
                    defaultValue = Screen.JournalEditor.NEW_ENTRY
                }
            ),
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(TRANSITION_DURATION),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(TRANSITION_DURATION),
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
            },
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong(Screen.JournalEditor.ARG_ENTRY_ID)
                ?: Screen.JournalEditor.NEW_ENTRY

            JournalEditorScreen(
                entryId  = entryId,
                onBack   = { navController.popBackStack() },
            )
        }

        // ── Mood Check-In ─────────────────────────────────────────────────────
        composable(
            route = Screen.MoodCheckIn.route,
            arguments = listOf(
                navArgument(Screen.MoodCheckIn.ARG_MOOD) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(TRANSITION_DURATION),
                    initialOffsetY = { it / 2 }
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(TRANSITION_DURATION),
                    targetOffsetY = { it / 2 }
                ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
            },
        ) { backStackEntry ->
            val preselectedMood = backStackEntry.arguments?.getString(Screen.MoodCheckIn.ARG_MOOD)
            MoodCheckInScreen(
                preselectedMood = preselectedMood,
                onComplete = { navController.popBackStack() },
                onWriteEntry = {
                    navController.popBackStack()
                    navController.navigate(Screen.JournalEditor.createRoute())
                }
            )
        }

        // ── Garden ────────────────────────────────────────────────────────────
        composable(Screen.Garden.route) {
            GardenScreen()
        }

        // ── Insights ──────────────────────────────────────────────────────────
        composable(Screen.Insights.route) {
            InsightsScreen()
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION),
                    initialOffsetX = { it }
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION),
                    targetOffsetX = { it }
                ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
            }
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
