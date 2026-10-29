package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BudgetProgressItem(
    val budget: BudgetEntity,
    val spent: Double,
    val remaining: Double,
    val percentUsed: Float,
    val isOverBudget: Boolean
)

data class AuraUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val currencySymbol: String = "$",
    val currencyCode: String = "USD",
    val themeMode: String = "DARK",
    val hasCompletedOnboarding: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val isBalanceVisible: Boolean = true,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null,
    val selectedTypeFilter: String? = null,
    val currentScreen: String = "MAIN", // MAIN, SETTINGS, ONBOARDING
    val isLoading: Boolean = false,
    val userNotification: String? = null
) {
    val totalIncome: Double
        get() = transactions
            .filter { it.type == TransactionType.INCOME.name }
            .sumOf { it.amount }

    val totalExpenses: Double
        get() = transactions
            .filter { it.type == TransactionType.EXPENSE.name }
            .sumOf { it.amount }

    val netWorth: Double
        get() = totalIncome - totalExpenses

    val savingsRate: Int
        get() = if (totalIncome > 0) {
            (((totalIncome - totalExpenses) / totalIncome) * 100).coerceIn(0.0, 100.0).toInt()
        } else 0

    val activeSubscriptionsMonthlyBurn: Double
        get() = subscriptions
            .filter { it.isActive }
            .sumOf {
                when (it.billingCycle.lowercase()) {
                    "yearly" -> it.amount / 12.0
                    "weekly" -> it.amount * 4.33
                    else -> it.amount
                }
            }

    val upcomingBillsCount: Int
        get() {
            val now = System.currentTimeMillis()
            val sevenDays = 7L * 24 * 60 * 60 * 1000
            return subscriptions.count { it.isActive && it.nextBillingTimestamp in now..(now + sevenDays) }
        }

    val categorySpendBreakdown: List<CategorySpend>
        get() {
            val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE.name }
            val total = expenseTransactions.sumOf { it.amount }
            if (total == 0.0) return emptyList()

            return expenseTransactions
                .groupBy { it.category }
                .map { (catStr, list) ->
                    val sum = list.sumOf { it.amount }
                    CategorySpend(
                        category = FinanceCategory.fromString(catStr),
                        totalAmount = sum,
                        percentage = (sum / total).toFloat()
                    )
                }
                .sortedByDescending { it.totalAmount }
        }

    val budgetProgressList: List<BudgetProgressItem>
        get() {
            return budgets.map { budget ->
                val spent = transactions
                    .filter { it.type == TransactionType.EXPENSE.name && it.category == budget.category }
                    .sumOf { it.amount }
                val remaining = (budget.monthlyLimit - spent).coerceAtLeast(0.0)
                val percent = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
                BudgetProgressItem(
                    budget = budget,
                    spent = spent,
                    remaining = remaining,
                    percentUsed = percent.coerceIn(0f, 2f),
                    isOverBudget = spent > budget.monthlyLimit
                )
            }
        }

    val filteredTransactions: List<TransactionEntity>
        get() {
            return transactions.filter { item ->
                val matchesQuery = searchQuery.isBlank() ||
                        item.title.contains(searchQuery, ignoreCase = true) ||
                        item.note.contains(searchQuery, ignoreCase = true) ||
                        item.category.contains(searchQuery, ignoreCase = true)

                val matchesCategory = selectedCategoryFilter == null || item.category == selectedCategoryFilter
                val matchesType = selectedTypeFilter == null || item.type == selectedTypeFilter

                matchesQuery && matchesCategory && matchesType
            }
        }

    val cashFlowPoints: List<CashFlowPoint>
        get() {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val points = mutableListOf<CashFlowPoint>()

            for (i in 6 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val end = start + (24L * 60 * 60 * 1000)

                val dayIncome = transactions
                    .filter { it.timestamp in start until end && it.type == TransactionType.INCOME.name }
                    .sumOf { it.amount }

                val dayExpense = transactions
                    .filter { it.timestamp in start until end && it.type == TransactionType.EXPENSE.name }
                    .sumOf { it.amount }

                points.add(
                    CashFlowPoint(
                        label = dateFormat.format(Date(start)),
                        income = dayIncome,
                        expense = dayExpense
                    )
                )
            }
            return points
        }
}

class AuraViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuraUiState())
    val uiState: StateFlow<AuraUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getAllTransactions(),
                repository.getAllBudgets(),
                repository.getAllSubscriptions()
            ) { trans, budg, subs ->
                Triple(trans, budg, subs)
            }.collect { (trans, budg, subs) ->
                _uiState.update { current ->
                    current.copy(
                        transactions = trans,
                        budgets = budg,
                        subscriptions = subs
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                repository.currencySymbol,
                repository.currencyCode,
                repository.themeMode
            ) { sym, code, theme ->
                Triple(sym, code, theme)
            }.collect { (sym, code, theme) ->
                _uiState.update { current ->
                    current.copy(
                        currencySymbol = sym,
                        currencyCode = code,
                        themeMode = theme
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                repository.hasCompletedOnboarding,
                repository.isBiometricEnabled
            ) { onb, bio ->
                Pair(onb, bio)
            }.collect { (onb, bio) ->
                _uiState.update { current ->
                    current.copy(
                        hasCompletedOnboarding = onb,
                        isBiometricEnabled = bio
                    )
                }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun navigateTo(screen: String) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setSearchQuery(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun setCategoryFilter(category: String?) {
        _uiState.update {
            it.copy(selectedCategoryFilter = if (it.selectedCategoryFilter == category) null else category)
        }
    }

    fun setTypeFilter(type: String?) {
        _uiState.update {
            it.copy(selectedTypeFilter = if (it.selectedTypeFilter == type) null else type)
        }
    }

    fun toggleBalanceVisibility() {
        _uiState.update { it.copy(isBalanceVisible = !it.isBalanceVisible) }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: FinanceCategory,
        account: AccountType,
        timestamp: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = type.name,
                    category = category.name,
                    account = account.name,
                    timestamp = timestamp,
                    note = note
                )
            )
            showNotification("Transaction added")
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
            showNotification("Transaction deleted")
        }
    }

    fun addBudget(category: FinanceCategory, monthlyLimit: Double) {
        viewModelScope.launch {
            repository.insertBudget(
                BudgetEntity(
                    category = category.name,
                    monthlyLimit = monthlyLimit,
                    period = "Monthly"
                )
            )
            showNotification("Budget set for ${category.title}")
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudgetById(id)
            showNotification("Budget removed")
        }
    }

    fun addSubscription(
        name: String,
        amount: Double,
        billingCycle: String,
        nextBillingTimestamp: Long,
        category: FinanceCategory
    ) {
        viewModelScope.launch {
            repository.insertSubscription(
                SubscriptionEntity(
                    name = name,
                    amount = amount,
                    billingCycle = billingCycle,
                    nextBillingTimestamp = nextBillingTimestamp,
                    category = category.name,
                    isActive = true
                )
            )
            showNotification("Subscription added")
        }
    }

    fun toggleSubscriptionActive(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.updateSubscription(subscription.copy(isActive = !subscription.isActive))
        }
    }

    fun deleteSubscription(id: Long) {
        viewModelScope.launch {
            repository.deleteSubscriptionById(id)
            showNotification("Subscription removed")
        }
    }

    fun setCurrency(currency: CurrencyOption) {
        viewModelScope.launch {
            repository.setCurrency(currency.code, currency.symbol)
            showNotification("Currency set to ${currency.code} (${currency.symbol})")
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometricEnabled(enabled)
            showNotification(if (enabled) "Biometric lock enabled" else "Biometric lock disabled")
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
            _uiState.update { it.copy(currentScreen = "MAIN") }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToDemoData()
            showNotification("Portfolio reset to demo data")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            showNotification("All records cleared")
        }
    }

    private fun showNotification(msg: String) {
        _uiState.update { it.copy(userNotification = msg) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }
}

class AuraViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuraViewModel::class.java)) {
            return AuraViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
