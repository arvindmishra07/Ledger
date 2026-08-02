package com.ledger.app.domain.repository


import com.ledger.app.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    fun getForMonth(month: Int, year: Int): Flow<List<Budget>>

    suspend fun getOverallBudgetAmount(month: Int, year: Int): Double?

    suspend fun setOverallBudget(amount: Double, month: Int, year: Int)

    suspend fun setCategoryBudget(categoryId: Long, amount: Double, month: Int, year: Int)

    suspend fun delete(budgetId: Long)
}