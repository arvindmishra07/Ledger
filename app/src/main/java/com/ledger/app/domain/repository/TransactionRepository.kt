package com.ledger.app.domain.repository


import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.CategorySpend
import com.ledger.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getAll(): Flow<List<Transaction>>

    fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>>

    fun getFiltered(
        start: Long,
        end: Long,
        categoryId: Long?,
        isExpense: Boolean?,
        sortBy: String
    ): Flow<List<Transaction>>

    fun search(query: String): Flow<List<Transaction>>

    fun getTotalExpense(start: Long, end: Long): Flow<Double>

    fun getTotalIncome(start: Long, end: Long): Flow<Double>

    fun getCategoryBreakdown(start: Long, end: Long, categories: List<Category>): Flow<List<CategorySpend>>

    suspend fun getLargestExpense(start: Long, end: Long): Transaction?

    suspend fun add(
        amount: Double,
        isExpense: Boolean,
        categoryId: Long,
        note: String,
        date: Long,
        tags: List<String>
    ): Long

    suspend fun update(transaction: Transaction)

    suspend fun delete(transaction: Transaction)

    suspend fun getCount(): Int

    suspend fun deleteAll()
}