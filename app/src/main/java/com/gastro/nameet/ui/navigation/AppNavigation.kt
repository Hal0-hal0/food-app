package com.gastro.nameet.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gastro.nameet.ui.screens.*
import com.gastro.nameet.util.RouteConstants

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // We get the current route cleanly by dropping any arguments
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/")

    val bottomNavRoutes = listOf(
        RouteConstants.HOME,
        RouteConstants.MAPS,
        RouteConstants.BOOKMARKS,
        RouteConstants.PROFILE
    )

    val showBottomNav = currentRoute in bottomNavRoutes

    if (showBottomNav) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                item(
                    selected = currentRoute == RouteConstants.HOME,
                    onClick = {
                        navController.navigate(RouteConstants.HOME) {
                            popUpTo(RouteConstants.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(if (currentRoute == RouteConstants.HOME) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                item(
                    selected = currentRoute == RouteConstants.MAPS,
                    onClick = {
                        navController.navigate(RouteConstants.MAPS) {
                            popUpTo(RouteConstants.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(if (currentRoute == RouteConstants.MAPS) Icons.Filled.Map else Icons.Outlined.Map, contentDescription = "Maps") },
                    label = { Text("Maps") }
                )
                item(
                    selected = currentRoute == RouteConstants.BOOKMARKS,
                    onClick = {
                        navController.navigate(RouteConstants.BOOKMARKS) {
                            popUpTo(RouteConstants.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(if (currentRoute == RouteConstants.BOOKMARKS) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Bookmarks") },
                    label = { Text("Bookmarks") }
                )
                item(
                    selected = currentRoute == RouteConstants.PROFILE,
                    onClick = {
                        navController.navigate(RouteConstants.PROFILE) {
                            popUpTo(RouteConstants.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(if (currentRoute == RouteConstants.PROFILE) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        ) {
            AppNavHost(navController = navController)
        }
    } else {
        AppNavHost(navController = navController)
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = RouteConstants.SPLASH
    ) {
        composable(RouteConstants.SPLASH) {
            SplashScreen(navController)
        }
        composable(RouteConstants.START) {
            StartScreen(navController)
        }
        composable(RouteConstants.LOGIN) {
            LoginScreen(navController)
        }
        composable(RouteConstants.REGISTER) {
            RegisterScreen(navController)
        }
        composable(RouteConstants.HOME) {
            HomeScreen(navController)
        }
        composable(RouteConstants.MAPS) {
            MapsScreen(navController)
        }
        composable(
            route = "${RouteConstants.PLACE_DETAILS}/{${RouteConstants.PLACE_ID_ARG}}",
            arguments = listOf(
                navArgument(RouteConstants.PLACE_ID_ARG) { type = NavType.IntType }
            )
        ) {
            PlaceDetailsScreen(navController)
        }
        composable(
            route = "${RouteConstants.SPECIALTIES}/{${RouteConstants.CATEGORY_ARG}}",
            arguments = listOf(
                navArgument(RouteConstants.CATEGORY_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString(RouteConstants.CATEGORY_ARG) ?: ""
            SpecialtiesScreen(navController, category)
        }
        composable(RouteConstants.BOOKMARKS) {
            BookmarksScreen(navController)
        }
        composable(RouteConstants.PROFILE) {
            ProfileScreen(navController)
        }
        composable(RouteConstants.SETTINGS) {
            SettingsScreen(navController)
        }
    }
}
