package com.example.madhu_marga_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.madhu_marga_2.ui.screens.*
import com.example.madhu_marga_2.ui.theme.Madhumarga2Theme
import com.example.madhu_marga_2.viewmodel.HiveViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Madhumarga2Theme {
                MadhuMargaApp()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "HOME", Icons.Default.Home)
    object Hives : Screen("hives", "HIVES", Icons.AutoMirrored.Filled.List)
    object Analytics : Screen("analytics", "DATA", Icons.Default.Analytics)
    object Calendar : Screen("calendar", "FLORA", Icons.Default.DateRange)
    object Alerts : Screen("alerts", "ALERTS", Icons.Default.Notifications)
}

@Composable
fun MadhuMargaApp() {
    val navController = rememberNavController()
    val viewModel: HiveViewModel = viewModel()
    val items = listOf(
        Screen.Dashboard,
        Screen.Hives,
        Screen.Analytics,
        Screen.Calendar,
        Screen.Alerts
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                tonalElevation = 0.dp,
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    windowInsets = WindowInsets.navigationBars,
                    tonalElevation = 0.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { 
                                AnimatedContent(
                                    targetState = selected,
                                    transitionSpec = {
                                        scaleIn(animationSpec = spring()) togetherWith scaleOut()
                                    }, label = ""
                                ) { isSelected ->
                                    Icon(
                                        screen.icon, 
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            label = { 
                                Text(
                                    screen.label, 
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Black else androidx.compose.ui.text.font.FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ) 
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { 
                fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(400))
            },
            exitTransition = { 
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -20 }, animationSpec = tween(400))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToFlora = { navController.navigate(Screen.Calendar.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                    onNavigateToHives = { navController.navigate(Screen.Hives.route) },
                    onAddHive = { navController.navigate("addHive") }
                )
            }
            composable(Screen.Hives.route) {
                HiveListScreen(
                    viewModel = viewModel,
                    onHiveClick = { hiveId: Long -> navController.navigate("hiveDetail/$hiveId") },
                    onAddHiveClick = { navController.navigate("addHive") }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Calendar.route) {
                FloraCalendarScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Alerts.route) {
                AlertsScreen(viewModel = viewModel)
            }
            composable("addHive") {
                AddHiveScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "hiveDetail/{hiveId}",
                arguments = listOf(navArgument("hiveId") { type = NavType.LongType })
            ) { backStackEntry ->
                val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: return@composable
                HiveDetailScreen(
                    hiveId = hiveId,
                    viewModel = viewModel,
                    onNavigateToInspections = { navController.navigate("inspections/$hiveId") },
                    onNavigateToHarvests = { navController.navigate("harvests/$hiveId") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "inspections/{hiveId}",
                arguments = listOf(navArgument("hiveId") { type = NavType.LongType })
            ) { backStackEntry ->
                val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: return@composable
                InspectionLogScreen(
                    hiveId = hiveId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "harvests/{hiveId}",
                arguments = listOf(navArgument("hiveId") { type = NavType.LongType })
            ) { backStackEntry ->
                val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: return@composable
                HarvestTrackerScreen(
                    hiveId = hiveId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
