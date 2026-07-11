package com.bloom.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bloom.app.ui.navigation.BloomBottomBar
import com.bloom.app.ui.navigation.BloomNavGraph
import com.bloom.app.ui.navigation.Screen
import com.bloom.app.ui.theme.BloomTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloom.app.util.ThemePreference

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Determine start destination before first frame renders
        // Using runBlocking here is intentional — we need this value
        // synchronously to avoid a flash between onboarding and home.
        // DataStore reads are fast (in-memory after first read).
        val userPrefs = (application as BloomApplication).container.userPreferences
        val isOnboardingDone = runBlocking { userPrefs.isOnboardingComplete.first() }
        val nextDestination = if (isOnboardingDone) Screen.Home.route else Screen.Onboarding.route

        setContent {
            val themePref by userPrefs.themePreference.collectAsStateWithLifecycle(initialValue = ThemePreference.SYSTEM)
            BloomTheme(themePreference = themePref) {
                BloomApp(startDestination = Screen.Splash.route, nextDestination = nextDestination)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BloomApp — Root composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BloomApp(startDestination: String, nextDestination: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Screens where the bottom nav should be hidden
    val hideBottomNavRoutes = setOf(
        Screen.Splash.route,
        Screen.Onboarding.route,
        Screen.MoodCheckIn.route,
        Screen.JournalEditor.route.substringBefore("/{"),
        Screen.Settings.route,
    )

    val showBottomNav = currentDestination?.route?.let { route ->
        hideBottomNavRoutes.none { route.startsWith(it) }
    } ?: false

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),  // We handle insets manually
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter   = fadeIn(),
                exit    = fadeOut(),
            ) {
                BloomBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        BloomNavGraph(
            navController    = navController,
            startDestination = startDestination,
            nextDestination  = nextDestination,
            modifier         = Modifier.padding(paddingValues),
        )
    }
}
