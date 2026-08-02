package com.ledger.app.di

import android.content.Context
import com.ledger.app.data.database.LedgerDatabase
import com.ledger.app.data.preferences.SettingsDataStore
import com.ledger.app.data.repository.BudgetRepositoryImpl
import com.ledger.app.data.repository.CategoryRepositoryImpl
import com.ledger.app.data.repository.TransactionRepositoryImpl
import com.ledger.app.domain.repository.BudgetRepository
import com.ledger.app.domain.repository.CategoryRepository
import com.ledger.app.domain.repository.TransactionRepository

/**
 * Simple hand-rolled DI container (no Hilt/Koin dependency needed).
 * Provides singletons for database, repositories, and preferences.
 */
class AppContainer(context: Context) {

    private val database: LedgerDatabase by lazy {
        LedgerDatabase.getInstance(context)
    }

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(context)
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(database.transactionDao(), database.categoryDao())
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    val budgetRepository: BudgetRepository by lazy {
        BudgetRepositoryImpl(database.budgetDao())
    }
}