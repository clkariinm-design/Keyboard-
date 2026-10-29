package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccountType
import com.example.data.model.BudgetEntity
import com.example.data.model.FinanceCategory
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        SubscriptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AuraDatabase? = null

        fun getDatabase(context: Context): AuraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraDatabase::class.java,
                    "aura_finance_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { seedInitialData(it) }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(database: AuraDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            // Prepopulate Transactions
            val sampleTransactions = listOf(
                TransactionEntity(
                    title = "Tech Lead Bi-Weekly Salary",
                    amount = 4850.00,
                    type = TransactionType.INCOME.name,
                    category = FinanceCategory.SALARY.name,
                    account = AccountType.CHECKING.name,
                    timestamp = now - (dayMs * 1),
                    note = "Direct Deposit"
                ),
                TransactionEntity(
                    title = "Whole Foods Organic Market",
                    amount = 142.80,
                    type = TransactionType.EXPENSE.name,
                    category = FinanceCategory.GROCERIES.name,
                    account = AccountType.CREDIT_CARD.name,
                    timestamp = now - (dayMs * 2),
                    note = "Weekly pantry run & fresh produce"
                ),
                TransactionEntity(
                    title = "Freelance UI/UX System Design",
                    amount = 1250.00,
                    type = TransactionType.INCOME.name,
                    category = FinanceCategory.FREELANCE.name,
                    account = AccountType.CHECKING.name,
                    timestamp = now - (dayMs * 3),
                    note = "Client milestone payment"
                ),
                TransactionEntity(
                    title = "Blue Bottle Artisan Espresso",
                    amount = 9.25,
                    type = TransactionType.EXPENSE.name,
                    category = FinanceCategory.DINING.name,
                    account = AccountType.CREDIT_CARD.name,
                    timestamp = now - (dayMs * 3),
                    note = "Cortado & pastry"
                ),
                TransactionEntity(
                    title = "Vanguard S&P 500 ETF (VOO)",
                    amount = 600.00,
                    type = TransactionType.EXPENSE.name,
                    category = FinanceCategory.INVESTMENTS.name,
                    account = AccountType.INVESTMENT.name,
                    timestamp = now - (dayMs * 5),
                    note = "Automated dollar-cost averaging"
                ),
                TransactionEntity(
                    title = "Equinox Luxury Fitness Club",
                    amount = 185.00,
                    type = TransactionType.EXPENSE.name,
                    category = FinanceCategory.HEALTH.name,
                    account = AccountType.CREDIT_CARD.name,
                    timestamp = now - (dayMs * 6),
                    note = "Monthly membership & spa"
                ),
                TransactionEntity(
                    title = "Apple Store Online - Magic Trackpad",
                    amount = 129.00,
                    type = TransactionType.EXPENSE.name,
                    category = FinanceCategory.SHOPPING.name,
                    account = AccountType.CREDIT_CARD.name,
                    timestamp = now - (dayMs * 7),
                    note = "Desk upgrade accessories"
                ),
                TransactionEntity(
                    title = "City Metro Transit Card Recharge",
                    amount = 45.00,
                    type = TransactionType.EXPENSE.name,
                    category = FinanceCategory.TRANSPORT.name,
                    account = AccountType.CHECKING.name,
                    timestamp = now - (dayMs * 8),
                    note = "Express pass refill"
                ),
                TransactionEntity(
                    title = "City Power & High-Speed Fiber",
                    amount = 118.50,
                    type = TransactionType.EXPENSE.name,
                    category = FinanceCategory.BILLS.name,
                    account = AccountType.CHECKING.name,
                    timestamp = now - (dayMs * 10),
                    note = "Monthly utilities payment"
                )
            )

            for (t in sampleTransactions) {
                database.transactionDao().insertTransaction(t)
            }

            // Prepopulate Budgets
            val sampleBudgets = listOf(
                BudgetEntity(category = FinanceCategory.GROCERIES.name, monthlyLimit = 650.00, period = "Monthly"),
                BudgetEntity(category = FinanceCategory.DINING.name, monthlyLimit = 400.00, period = "Monthly"),
                BudgetEntity(category = FinanceCategory.SHOPPING.name, monthlyLimit = 350.00, period = "Monthly"),
                BudgetEntity(category = FinanceCategory.TRANSPORT.name, monthlyLimit = 200.00, period = "Monthly"),
                BudgetEntity(category = FinanceCategory.ENTERTAINMENT.name, monthlyLimit = 250.00, period = "Monthly"),
                BudgetEntity(category = FinanceCategory.HEALTH.name, monthlyLimit = 300.00, period = "Monthly")
            )

            for (b in sampleBudgets) {
                database.budgetDao().insertBudget(b)
            }

            // Prepopulate Subscriptions
            val sampleSubscriptions = listOf(
                SubscriptionEntity(
                    name = "Spotify Premium Duo",
                    amount = 14.99,
                    billingCycle = "Monthly",
                    nextBillingTimestamp = now + (dayMs * 4),
                    category = FinanceCategory.ENTERTAINMENT.name,
                    isActive = true
                ),
                SubscriptionEntity(
                    name = "Apple One Premier & iCloud+",
                    amount = 37.95,
                    billingCycle = "Monthly",
                    nextBillingTimestamp = now + (dayMs * 8),
                    category = FinanceCategory.ENTERTAINMENT.name,
                    isActive = true
                ),
                SubscriptionEntity(
                    name = "Netflix 4K Ultra Plan",
                    amount = 22.99,
                    billingCycle = "Monthly",
                    nextBillingTimestamp = now + (dayMs * 12),
                    category = FinanceCategory.ENTERTAINMENT.name,
                    isActive = true
                ),
                SubscriptionEntity(
                    name = "Figma Professional & AI",
                    amount = 15.00,
                    billingCycle = "Monthly",
                    nextBillingTimestamp = now + (dayMs * 15),
                    category = FinanceCategory.FREELANCE.name,
                    isActive = true
                ),
                SubscriptionEntity(
                    name = "AWS Cloud Infrastructure",
                    amount = 48.20,
                    billingCycle = "Monthly",
                    nextBillingTimestamp = now + (dayMs * 22),
                    category = FinanceCategory.BILLS.name,
                    isActive = true
                )
            )

            for (s in sampleSubscriptions) {
                database.subscriptionDao().insertSubscription(s)
            }
        }
    }
}
