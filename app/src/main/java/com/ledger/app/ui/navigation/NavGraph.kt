package com.ledger.app.ui.navigation


import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.ledger.app.di.AppContainer
import com.ledger.app.ui.addtransaction.AddTransactionScreen
import com.ledger.app.ui.addtransaction.AddTransactionViewModel
import com.ledger.app.ui.budgets.BudgetsScreen
import com.ledger.app.ui.budgets.BudgetsViewModel
import com.ledger.app.ui.categories.CategoriesScreen
import com.ledger.app.ui.categories.CategoriesViewModel
import com.ledger.app.ui.dashboard.DashboardScreen
import com.ledger.app.ui.dashboard.DashboardViewModel
import com.ledger.app.ui.history.HistoryScreen
import com.ledger.app.ui.history.HistoryViewModel
import com.ledger.app.ui.insights.InsightsScreen
import com.ledger.app.ui.insights.InsightsViewModel
import com.ledger.app.ui.reports.ReportsScreen
import com.ledger.app.ui.reports.ReportsViewModel
import com.ledger.app.ui.settings.SettingsScreen
import com.ledger.app.ui.settings.SettingsViewModel

private data class NavIcon(val selected: androidx.compose.ui.graphics.vector.ImageVector, val unselected: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomNavIcons = mapOf(
    BottomNavItem.DASHBOARD to NavIcon(Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem.HISTORY to NavIcon(Icons.Filled.Receipt, Icons.Outlined.Receipt),
    BottomNavItem.BUDGETS to NavIcon(Icons.Filled.PieChart, Icons.Outlined.PieChart),
    BottomNavItem.INSIGHTS to NavIcon(Icons.Filled.Insights, Icons.Outlined.Insights),
    BottomNavItem.SETTINGS to NavIcon(Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun NavGraph(container: AppContainer) {
    val navController = rememberNavController()

    val bottomBarRoutes = remember {
        BottomNavItem.values().map { it.screen.route }.toSet()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar {
                    BottomNavItem.values().forEach { item ->
                        val selected = currentRoute == item.screen.route
                        val icons = bottomNavIcons[item]!!
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) icons.selected else icons.unselected,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 6 } },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 6 } }
        ) {
            composable(Screen.Splash.route) {
                com.ledger.app.ui.splash.SplashScreen(
                    onFinished = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                val vm = remember {
                    DashboardViewModel(
                        getDashboardSummaryUseCase = container.let {
                            com.ledger.app.domain.usecase.GetDashboardSummaryUseCase(
                                it.transactionRepository, it.categoryRepository, it.budgetRepository
                            )
                        },
                        settingsDataStore = container.settingsDataStore
                    )
                }
                DashboardScreen(
                    viewModel = vm,
                    onAddTransaction = { navController.navigate(Screen.AddTransaction.createRoute()) },
                    onViewHistory = { navController.navigate(Screen.History.route) },
                    onViewInsights = { navController.navigate(Screen.Insights.route) },
                    onViewBudgets = { navController.navigate(Screen.Budgets.route) },
                    onViewReports = { navController.navigate(Screen.Reports.route) }
                )
            }

            composable(
                route = Screen.AddTransaction.route,
                arguments = listOf(navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: -1L
                val vm = remember {
                    AddTransactionViewModel(
                        transactionRepository = container.transactionRepository,
                        categoryRepository = container.categoryRepository,
                        editingTransactionId = if (transactionId == -1L) null else transactionId
                    )
                }
                AddTransactionScreen(
                    viewModel = vm,
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Screen.Categories.route) {
                val vm = remember { CategoriesViewModel(container.categoryRepository) }
                CategoriesScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.Budgets.route) {
                val vm = remember {
                    BudgetsViewModel(
                        budgetRepository = container.budgetRepository,
                        categoryRepository = container.categoryRepository,
                        transactionRepository = container.transactionRepository
                    )
                }
                BudgetsScreen(viewModel = vm)
            }

            composable(Screen.History.route) {
                val vm = remember {
                    HistoryViewModel(
                        transactionRepository = container.transactionRepository,
                        categoryRepository = container.categoryRepository
                    )
                }
                HistoryScreen(
                    viewModel = vm,
                    onTransactionClick = { id -> navController.navigate(Screen.AddTransaction.createRoute(id)) }
                )
            }

            composable(Screen.Insights.route) {
                val vm = remember {
                    InsightsViewModel(
                        getInsightsUseCase = com.ledger.app.domain.usecase.GetInsightsUseCase(
                            container.transactionRepository, container.categoryRepository
                        )
                    )
                }
                InsightsScreen(viewModel = vm)
            }

            composable(Screen.Reports.route) {
                val vm = remember {
                    ReportsViewModel(
                        transactionRepository = container.transactionRepository,
                        categoryRepository = container.categoryRepository
                    )
                }
                ReportsScreen(viewModel = vm)
            }

            composable(Screen.Settings.route) {
                val vm = remember { SettingsViewModel(container.settingsDataStore, container.transactionRepository) }
                SettingsScreen(
                    viewModel = vm,
                    onManageCategories = { navController.navigate(Screen.Categories.route) },
                    onManageBudgets = { navController.navigate(Screen.Budgets.route) }
                )
            }
        }
    }
}