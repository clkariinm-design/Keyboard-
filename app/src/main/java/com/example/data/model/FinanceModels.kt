package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.*

enum class TransactionType(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer")
}

enum class FinanceCategory(
    val title: String,
    val iconName: String,
    val color: Color
) {
    GROCERIES("Groceries", "shopping_cart", CatGroceriesColor),
    DINING("Dining & Drinks", "restaurant", CatDiningColor),
    SHOPPING("Shopping", "local_mall", CatShoppingColor),
    TRANSPORT("Transport & Fuel", "directions_car", CatTransportColor),
    HOUSING("Housing & Rent", "home", CatHousingColor),
    ENTERTAINMENT("Entertainment", "movie", CatEntertainmentColor),
    HEALTH("Health & Fitness", "fitness_center", CatHealthColor),
    SALARY("Salary & Wages", "payments", CatSalaryColor),
    FREELANCE("Freelance & Business", "work", CatFreelanceColor),
    INVESTMENTS("Investments", "trending_up", CatInvestmentsColor),
    BILLS("Bills & Utilities", "receipt_long", CatBillsColor),
    OTHER("Miscellaneous", "category", CatOtherColor);

    companion object {
        fun fromString(name: String): FinanceCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class AccountType(val title: String, val subtitle: String) {
    CHECKING("Checking Account", "Primary Checking"),
    SAVINGS("High-Yield Savings", "Reserve Wealth"),
    CREDIT_CARD("Credit Card", "Daily Points Card"),
    INVESTMENT("Investment Portfolio", "Brokerage & Index"),
    CASH("Physical Cash", "Wallet Cash")
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // EXPENSE, INCOME, TRANSFER
    val category: String, // FinanceCategory.name
    val account: String, // AccountType.name
    val timestamp: Long,
    val note: String = ""
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // FinanceCategory.name
    val monthlyLimit: Double,
    val period: String // e.g. "2026-10" or "Monthly"
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val billingCycle: String, // "Monthly", "Yearly", "Weekly"
    val nextBillingTimestamp: Long,
    val category: String,
    val isActive: Boolean = true,
    val iconKey: String = "sub_default"
)

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
)

val SupportedCurrencies = listOf(
    CurrencyOption("USD", "$", "US Dollar"),
    CurrencyOption("EUR", "€", "Euro"),
    CurrencyOption("GBP", "£", "British Pound"),
    CurrencyOption("JPY", "¥", "Japanese Yen"),
    CurrencyOption("CAD", "CA$", "Canadian Dollar"),
    CurrencyOption("AUD", "AU$", "Australian Dollar"),
    CurrencyOption("INR", "₹", "Indian Rupee"),
    CurrencyOption("CHF", "CHF", "Swiss Franc")
)

data class CategorySpend(
    val category: FinanceCategory,
    val totalAmount: Double,
    val percentage: Float
)

data class CashFlowPoint(
    val label: String,
    val income: Double,
    val expense: Double
)
