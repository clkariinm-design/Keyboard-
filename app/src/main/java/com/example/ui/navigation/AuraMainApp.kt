package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.AddBudgetBottomSheet
import com.example.ui.components.AddSubscriptionBottomSheet
import com.example.ui.components.AddTransactionBottomSheet
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuraViewModel

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun AuraMainApp(viewModel: AuraViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val isDark = when (uiState.themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    MyApplicationTheme(darkTheme = isDark) {
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(uiState.userNotification) {
            uiState.userNotification?.let { msg ->
                snackbarHostState.showSnackbar(msg)
                viewModel.clearNotification()
            }
        }

        var showAddTransactionSheet by remember { mutableStateOf(false) }
        var showAddBudgetSheet by remember { mutableStateOf(false) }
        var showAddSubscriptionSheet by remember { mutableStateOf(false) }

        if (!uiState.hasCompletedOnboarding) {
            OnboardingScreen(viewModel = viewModel)
        } else if (uiState.currentScreen == "SETTINGS") {
            SettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.navigateTo("MAIN") }
            )
        } else {
            val navTabs = remember {
                listOf(
                    NavTabItem("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_dashboard"),
                    NavTabItem("Ledger", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "tab_transactions"),
                    NavTabItem("Budgets", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "tab_budgets"),
                    NavTabItem("Subs", Icons.Filled.CreditCard, Icons.Outlined.CreditCard, "tab_subscriptions"),
                    NavTabItem("Insights", Icons.Filled.Insights, Icons.Outlined.Insights, "tab_analytics")
                )
            }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.testTag("aura_bottom_nav")
                    ) {
                        navTabs.forEachIndexed { index, tab ->
                            val isSelected = uiState.selectedTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(index) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                },
                contentWindowInsets = WindowInsets.safeDrawing,
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (uiState.selectedTab) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            onOpenAddTransaction = { showAddTransactionSheet = true },
                            onOpenAddBudget = { showAddBudgetSheet = true },
                            onOpenAddSubscription = { showAddSubscriptionSheet = true },
                            onOpenSettings = { viewModel.navigateTo("SETTINGS") }
                        )
                        1 -> TransactionsScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            onOpenAddTransaction = { showAddTransactionSheet = true }
                        )
                        2 -> BudgetsScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            onOpenAddBudget = { showAddBudgetSheet = true }
                        )
                        3 -> SubscriptionsScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            onOpenAddSubscription = { showAddSubscriptionSheet = true }
                        )
                        4 -> AnalyticsScreen(
                            uiState = uiState
                        )
                    }
                }
            }

            // Bottom Sheets
            if (showAddTransactionSheet) {
                AddTransactionBottomSheet(
                    currencySymbol = uiState.currencySymbol,
                    onDismiss = { showAddTransactionSheet = false },
                    onSave = { title, amount, type, category, account, timestamp, note ->
                        viewModel.addTransaction(title, amount, type, category, account, timestamp, note)
                    }
                )
            }

            if (showAddBudgetSheet) {
                AddBudgetBottomSheet(
                    currencySymbol = uiState.currencySymbol,
                    onDismiss = { showAddBudgetSheet = false },
                    onSave = { category, limit ->
                        viewModel.addBudget(category, limit)
                    }
                )
            }

            if (showAddSubscriptionSheet) {
                AddSubscriptionBottomSheet(
                    currencySymbol = uiState.currencySymbol,
                    onDismiss = { showAddSubscriptionSheet = false },
                    onSave = { name, amount, cycle, nextDate, category ->
                        viewModel.addSubscription(name, amount, cycle, nextDate, category)
                    }
                )
            }
        }
    }
}
