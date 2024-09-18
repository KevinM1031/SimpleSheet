package com.example.simplesheet.ui

import DashboardScreen
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.simplesheet.ui.AccountManagement.AccountManagementScreen
import com.example.simplesheet.ui.sheet.SheetScreen

enum class SimpleSheetScreen() {
    AccountManagement,
    Dashboard,
    Sheet,
}

@Composable
fun SimpleSheetNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = SimpleSheetScreen.AccountManagement.name,
        modifier = modifier
    ) {
        composable(route = SimpleSheetScreen.AccountManagement.name) {
            BackHandler(true) {}
            AccountManagementScreen(
                onSignedIn = { navController.navigate(SimpleSheetScreen.Dashboard.name) },
            )
        }
        composable(route = SimpleSheetScreen.Dashboard.name) {
            BackHandler(true) {}
            DashboardScreen(
                onSheetClicked = { navController.navigate("${SimpleSheetScreen.Sheet.name}/$it") },
                onSignedOut = {
                    navController.navigate(route ?: SimpleSheetScreen.AccountManagement.name) {
                        launchSingleTop = true
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "${SimpleSheetScreen.Sheet.name}/{sheetId}",
            arguments = listOf(
                navArgument("sheetId") { type = NavType.StringType },
            )
        ) {
            SheetScreen(
                onBackRequested = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(route ?: SimpleSheetScreen.AccountManagement.name) {
                        launchSingleTop = true
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
