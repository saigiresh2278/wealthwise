package com.example.wealthwiseai.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Expenses : Screen("transactions_management")
    object Advisor : Screen("advisor")
    object Goals : Screen("goals")
    object Reports : Screen("reports")
    object Learn : Screen("learn")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object DatabaseExplorer : Screen("db_explorer")
}
