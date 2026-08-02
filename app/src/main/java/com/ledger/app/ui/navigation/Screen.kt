package com.ledger.app.ui.navigation


sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AddTransaction : Screen("add_transaction?transactionId={transactionId}") {
        fun createRoute(transactionId: Long? = null) =
            "add_transaction?transactionId=${transactionId ?: -1L}"
    }
    data object Categories : Screen("categories")
    data object Budgets : Screen("budgets")
    data object History : Screen("history")
    data object Insights : Screen("insights")
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
}

/** Bottom navigation destinations (subset of all screens). */
enum class BottomNavItem(val screen: Screen, val label: String) {
    DASHBOARD(Screen.Dashboard, "Home"),
    HISTORY(Screen.History, "History"),
    BUDGETS(Screen.Budgets, "Budgets"),
    INSIGHTS(Screen.Insights, "Insights"),
    SETTINGS(Screen.Settings, "Settings")
}