package com.annhonii.cloudygallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.annhonii.cloudygallery.ui.screens.CloudSyncScreen
import com.annhonii.cloudygallery.ui.screens.GalleryScreen
import com.annhonii.cloudygallery.ui.screens.OnboardingScreen
import com.annhonii.cloudygallery.ui.screens.PhotoViewerScreen
import com.annhonii.cloudygallery.ui.screens.SettingsScreen
import com.annhonii.cloudygallery.ui.theme.CloudyGalleryTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Gallery : Screen("gallery", "Gallery", Icons.Filled.Collections)
    object Cloud : Screen("cloud", "Cloud", Icons.Filled.Cloud)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudyGalleryTheme {
                CloudyGalleryNavHost()
            }
        }
    }
}

@Composable
fun CloudyGalleryNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavScreens = listOf(Screen.Gallery, Screen.Cloud, Screen.Settings)
    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                ) {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label
                                )
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFF1A1A1A)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "gallery",
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(tween(300)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(300)
                )
            },
            exitTransition = {
                fadeOut(tween(300)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(300)
                )
            },
            popEnterTransition = {
                fadeIn(tween(300)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(300)
                )
            },
            popExitTransition = {
                fadeOut(tween(300)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(300)
                )
            }
        ) {
            composable(Screen.Gallery.route) {
                GalleryScreen(
                    onMediaClick = { mediaId ->
                        navController.navigate("viewer/$mediaId")
                    },
                    onNavigateToOnboarding = {
                        navController.navigate("onboarding")
                    }
                )
            }
            composable(Screen.Cloud.route) {
                CloudSyncScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = "viewer/{mediaId}",
                arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
            ) { backStackEntry ->
                val mediaId = backStackEntry.arguments?.getLong("mediaId") ?: 0L
                PhotoViewerScreen(
                    mediaId = mediaId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("onboarding") {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Gallery.route) {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
