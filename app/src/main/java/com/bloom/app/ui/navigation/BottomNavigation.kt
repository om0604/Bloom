package com.bloom.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Navigation
//
// 4 tabs — Home, Journal, Garden, Insights.
// Mood check-in is NOT a tab — it's a contextual action from Home.
//
// Design decisions:
//   - No labels by default; icon + label on selected only
//     → cleaner, less visual noise
//   - NavigationBar background uses surface color (warm white/dark brown)
//   - Selected indicator uses primaryContainer (warm amber tint)
//   - No divider line — Bloom doesn't use hard separators
// ─────────────────────────────────────────────────────────────────────────────

data class BottomNavItem(
    val screen      : Screen,
    val icon        : ImageVector,
    val label       : String,
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home,     Icons.Outlined.Home,          "Home"),
    BottomNavItem(Screen.Journal,  Icons.Outlined.Book,          "Journal"),
    BottomNavItem(Screen.Garden,   Icons.Outlined.Eco,           "Garden"),
    BottomNavItem(Screen.Insights, Icons.Outlined.AutoAwesome,   "Insights"),
)

@Composable
fun BloomBottomBar(
    currentDestination  : NavDestination?,
    onNavigate          : (Screen) -> Unit,
) {
    NavigationBar(
        containerColor  = MaterialTheme.colorScheme.surface,
        tonalElevation  = 0.dp,    // No shadow — clean, flat bottom bar
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == item.screen.route
            } == true

            NavigationBarItem(
                selected    = isSelected,
                onClick     = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text  = item.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                alwaysShowLabel = false,    // Only show label when selected
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor       = MaterialTheme.colorScheme.primary,
                    selectedTextColor       = MaterialTheme.colorScheme.primary,
                    indicatorColor          = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        }
    }
}
