package com.ledger.app.data.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ledger.app.data.database.dao.BudgetDao
import com.ledger.app.data.database.dao.CategoryDao
import com.ledger.app.data.database.dao.TagDao
import com.ledger.app.data.database.dao.TransactionDao
import com.ledger.app.data.database.entity.BudgetEntity
import com.ledger.app.data.database.entity.CategoryEntity
import com.ledger.app.data.database.entity.TagEntity
import com.ledger.app.data.database.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        TagEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LedgerDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: LedgerDatabase? = null

        fun getInstance(context: Context): LedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LedgerDatabase::class.java,
                    "ledger_database"
                )
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /** Seeds default categories on first creation. */
    private class SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.categoryDao()?.insertAll(defaultCategories())
            }
        }

        private fun defaultCategories(): List<CategoryEntity> = listOf(
            CategoryEntity(name = "Food", iconKey = "food", colorHex = "#FF7043", sortOrder = 0, isDefault = true),
            CategoryEntity(name = "Groceries", iconKey = "groceries", colorHex = "#66BB6A", sortOrder = 1, isDefault = true),
            CategoryEntity(name = "Travel", iconKey = "travel", colorHex = "#42A5F5", sortOrder = 2, isDefault = true),
            CategoryEntity(name = "Bills", iconKey = "bills", colorHex = "#AB47BC", sortOrder = 3, isDefault = true),
            CategoryEntity(name = "Rent", iconKey = "rent", colorHex = "#EC407A", sortOrder = 4, isDefault = true),
            CategoryEntity(name = "Shopping", iconKey = "shopping", colorHex = "#FFCA28", sortOrder = 5, isDefault = true),
            CategoryEntity(name = "Entertainment", iconKey = "entertainment", colorHex = "#26C6DA", sortOrder = 6, isDefault = true),
            CategoryEntity(name = "Investment", iconKey = "investment", colorHex = "#26A69A", sortOrder = 7, isDefault = true),
            CategoryEntity(name = "Health", iconKey = "health", colorHex = "#EF5350", sortOrder = 8, isDefault = true),
            CategoryEntity(name = "Education", iconKey = "education", colorHex = "#5C6BC0", sortOrder = 9, isDefault = true),
            CategoryEntity(name = "Fuel", iconKey = "fuel", colorHex = "#8D6E63", sortOrder = 10, isDefault = true),
            CategoryEntity(name = "Salary", iconKey = "salary", colorHex = "#00C896", sortOrder = 11, isIncome = true, isDefault = true),
            CategoryEntity(name = "Freelance", iconKey = "freelance", colorHex = "#00ACC1", sortOrder = 12, isIncome = true, isDefault = true),
            CategoryEntity(name = "Refund", iconKey = "refund", colorHex = "#9CCC65", sortOrder = 13, isIncome = true, isDefault = true),
            CategoryEntity(name = "Gift", iconKey = "gift", colorHex = "#EC407A", sortOrder = 14, isIncome = true, isDefault = true),
            CategoryEntity(name = "Other", iconKey = "other", colorHex = "#78909C", sortOrder = 15, isDefault = true)
        )
    }
}