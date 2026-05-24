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
import androidx.navigation.NavGraph.Companion.findStartDestination
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
    
    // Route-pattern base (strip any "/" path args or "?" query args).
    val routePattern = navBackStackEntry?.destination?.route.orEmpty()
    val currentRoute = routePattern
        .substringBefore("/")
        .substringBefore("?")

    // Routes where the bottom nav should be HIDDEN (the auth flow). We invert
    // the check so any in-app screen — including detail screens, parameterised
    // Maps, etc. — always has Home/Maps/Bookmarks/Profile tap targets.
    val hideBottomNavRoutes = setOf(
        RouteConstants.SPLASH,
        RouteConstants.START,
        RouteConstants.LOGIN,
        RouteConstants.REGISTER
    )

    val showBottomNav = currentRoute.isNotBlank() && currentRoute !in hideBottomNavRoutes

    // Navigating to a tab from anywhere in the app should land you cleanly on
    // that tab with a single back-stack entry, even if you came in from a
    // chain of detail screens. We pop to the start destination of the graph
    // so this works regardless of where the user is.
    fun navigateToTab(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    if (showBottomNav) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                item(
                    selected = currentRoute == RouteConstants.HOME,
                    onClick = { navigateToTab(RouteConstants.HOME) },
                    icon = { Icon(if (currentRoute == RouteConstants.HOME) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                item(
                    selected = currentRoute == RouteConstants.MAPS,
                    onClick = { navigateToTab(RouteConstants.MAPS) },
                    icon = { Icon(if (currentRoute == RouteConstants.MAPS) Icons.Filled.Map else Icons.Outlined.Map, contentDescription = "Maps") },
                    label = { Text("Maps") }
                )
                item(
                    selected = currentRoute == RouteConstants.BOOKMARKS,
                    onClick = { navigateToTab(RouteConstants.BOOKMARKS) },
                    icon = { Icon(if (currentRoute == RouteConstants.BOOKMARKS) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Bookmarks") },
                    label = { Text("Bookmarks") }
                )
                item(
                    selected = currentRoute == RouteConstants.PROFILE,
                    onClick = { navigateToTab(RouteConstants.PROFILE) },
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
            route = "${RouteConstants.MAPS}?${RouteConstants.MAP_QUERY_ARG}={${RouteConstants.MAP_QUERY_ARG}}&${RouteConstants.LOCATE_PLACE_ID_ARG}={${RouteConstants.LOCATE_PLACE_ID_ARG}}",
            arguments = listOf(
                navArgument(RouteConstants.MAP_QUERY_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(RouteConstants.LOCATE_PLACE_ID_ARG) {
                    type = NavType.IntType
                    defaultValue = RouteConstants.NO_PLACE_ID
                }
            )
        ) { backStackEntry ->
            MapsScreen(
                navController = navController,
                initialSearchQuery = backStackEntry.arguments?.getString(RouteConstants.MAP_QUERY_ARG),
                initialLocatePlaceId = backStackEntry.arguments?.getInt(RouteConstants.LOCATE_PLACE_ID_ARG)
                    ?.takeIf { it != RouteConstants.NO_PLACE_ID }
            )
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
            route = "${RouteConstants.FOOD_DETAIL}/{${RouteConstants.FOOD_ID_ARG}}",
            arguments = listOf(
                navArgument(RouteConstants.FOOD_ID_ARG) { type = NavType.IntType }
            )
        ) {
            FoodDetailScreen(navController)
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
        composable(RouteConstants.ILONGGO_SPECIALTIES) {
            IlonggoSpecialtiesScreen(navController)
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
