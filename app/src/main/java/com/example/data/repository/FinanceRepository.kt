package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.AuraDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.SupportedCurrencies
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface FinanceRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)
    suspend fun deleteTransactionById(id: Long)

    fun getAllBudgets(): Flow<List<BudgetEntity>>
    suspend fun insertBudget(budget: BudgetEntity): Long
    suspend fun updateBudget(budget: BudgetEntity)
    suspend fun deleteBudget(budget: BudgetEntity)
    suspend fun deleteBudgetById(id: Long)

    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long
    suspend fun updateSubscription(subscription: SubscriptionEntity)
    suspend fun deleteSubscription(subscription: SubscriptionEntity)
    suspend fun deleteSubscriptionById(id: Long)

    // Preferences
    val currencySymbol: Flow<String>
    val currencyCode: Flow<String>
    val themeMode: Flow<String> // "SYSTEM", "DARK", "LIGHT"
    val hasCompletedOnboarding: Flow<Boolean>
    val isBiometricEnabled: Flow<Boolean>

    suspend fun setCurrency(code: String, symbol: String)
    suspend fun setThemeMode(mode: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)

    suspend fun resetToDemoData()
    suspend fun clearAllData()
}

class FinanceRepositoryImpl(
    private val database: AuraDatabase,
    private val context: Context
) : FinanceRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aura_settings_prefs", Context.MODE_PRIVATE)

    private val _currencySymbol = MutableStateFlow(prefs.getString("currency_symbol", "$") ?: "$")
    override val currencySymbol: Flow<String> = _currencySymbol.asStateFlow()

    private val _currencyCode = MutableStateFlow(prefs.getString("currency_code", "USD") ?: "USD")
    override val currencyCode: Flow<String> = _currencyCode.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "DARK") ?: "DARK")
    override val themeMode: Flow<String> = _themeMode.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean("onboarding_done", true))
    override val hasCompletedOnboarding: Flow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean("biometric_enabled", false))
    override val isBiometricEnabled: Flow<Boolean> = _isBiometricEnabled.asStateFlow()

    override fun getAllTransactions(): Flow<List<TransactionEntity>> =
        database.transactionDao().getAllTransactions()

    override suspend fun insertTransaction(transaction: TransactionEntity): Long =
        database.transactionDao().insertTransaction(transaction)

    override suspend fun updateTransaction(transaction: TransactionEntity) =
        database.transactionDao().updateTransaction(transaction)

    override suspend fun deleteTransaction(transaction: TransactionEntity) =
        database.transactionDao().deleteTransaction(transaction)

    override suspend fun deleteTransactionById(id: Long) =
        database.transactionDao().deleteById(id)

    override fun getAllBudgets(): Flow<List<BudgetEntity>> =
        database.budgetDao().getAllBudgets()

    override suspend fun insertBudget(budget: BudgetEntity): Long =
        database.budgetDao().insertBudget(budget)

    override suspend fun updateBudget(budget: BudgetEntity) =
        database.budgetDao().updateBudget(budget)

    override suspend fun deleteBudget(budget: BudgetEntity) =
        database.budgetDao().deleteBudget(budget)

    override suspend fun deleteBudgetById(id: Long) =
        database.budgetDao().deleteById(id)

    override fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> =
        database.subscriptionDao().getAllSubscriptions()

    override suspend fun insertSubscription(subscription: SubscriptionEntity): Long =
        database.subscriptionDao().insertSubscription(subscription)

    override suspend fun updateSubscription(subscription: SubscriptionEntity) =
        database.subscriptionDao().updateSubscription(subscription)

    override suspend fun deleteSubscription(subscription: SubscriptionEntity) =
        database.subscriptionDao().deleteSubscription(subscription)

    override suspend fun deleteSubscriptionById(id: Long) =
        database.subscriptionDao().deleteById(id)

    override suspend fun setCurrency(code: String, symbol: String) {
        prefs.edit().putString("currency_code", code).putString("currency_symbol", symbol).apply()
        _currencyCode.value = code
        _currencySymbol.value = symbol
    }

    override suspend fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_done", completed).apply()
        _hasCompletedOnboarding.value = completed
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    override suspend fun resetToDemoData() {
        clearAllData()
        AuraDatabase.seedInitialData(database)
    }

    override suspend fun clearAllData() {
        database.transactionDao().clearAll()
        database.budgetDao().clearAll()
        database.subscriptionDao().clearAll()
    }
}
