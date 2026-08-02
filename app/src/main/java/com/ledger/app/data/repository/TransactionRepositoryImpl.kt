package com.ledger.app.data.repository

import com.ledger.app.data.database.dao.CategoryDao
import com.ledger.app.data.database.dao.TransactionDao
import com.ledger.app.data.database.entity.CategoryEntity
import com.ledger.app.data.database.entity.TransactionEntity
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.CategorySpend
import com.ledger.app.domain.model.Transaction
import com.ledger.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) : TransactionRepository {

    override fun getAll(): Flow<List<Transaction>> {
        return combine(transactionDao.getAll(), categoryDao.getAll()) { txns, cats ->
            txns.map { it.toDomain(cats) }
        }
    }

    override fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>> {
        return combine(transactionDao.getByDateRange(start, end), categoryDao.getAll()) { txns, cats ->
            txns.map { it.toDomain(cats) }
        }
    }

    override fun getFiltered(
        start: Long,
        end: Long,
        categoryId: Long?,
        isExpense: Boolean?,
        sortBy: String
    ): Flow<List<Transaction>> {
        return combine(
            transactionDao.getFiltered(start, end, categoryId, isExpense, sortBy),
            categoryDao.getAll()
        ) { txns, cats -> txns.map { it.toDomain(cats) } }
    }

    override fun search(query: String): Flow<List<Transaction>> {
        return combine(transactionDao.search(query), categoryDao.getAll()) { txns, cats ->
            txns.map { it.toDomain(cats) }
        }
    }

    override fun getTotalExpense(start: Long, end: Long): Flow<Double> {
        return transactionDao.getTotalExpense(start, end)
    }

    override fun getTotalIncome(start: Long, end: Long): Flow<Double> {
        return transactionDao.getTotalIncome(start, end)
    }

    override fun getCategoryBreakdown(
        start: Long,
        end: Long,
        categories: List<Category>
    ): Flow<List<CategorySpend>> {
        return transactionDao.getCategoryTotals(start, end).map { totals ->
            val totalSum = totals.sumOf { it.total }
            totals.mapNotNull { ct ->
                categories.find { it.id == ct.categoryId }?.let { cat ->
                    CategorySpend(
                        category = cat,
                        total = ct.total,
                        percentage = if (totalSum > 0) (ct.total / totalSum).toFloat() else 0f
                    )
                }
            }
        }
    }

    override suspend fun getLargestExpense(start: Long, end: Long): Transaction? {
        val entity = transactionDao.getLargestExpense(start, end) ?: return null
        val cat = categoryDao.getById(entity.categoryId)
        return entity.toDomain(listOfNotNull(cat))
    }

    override suspend fun add(
        amount: Double,
        isExpense: Boolean,
        categoryId: Long,
        note: String,
        date: Long,
        tags: List<String>
    ): Long {
        val entity = TransactionEntity(
            amount = amount,
            isExpense = isExpense,
            categoryId = categoryId,
            note = note,
            date = date,
            tags = tags.joinToString(",")
        )
        return transactionDao.insert(entity)
    }

    override suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction.toEntity())
    }

    override suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction.toEntity())
    }

    override suspend fun getCount(): Int = transactionDao.getCount()

    override suspend fun deleteAll() = transactionDao.deleteAll()

    // --- Mapping helpers ---

    private fun TransactionEntity.toDomain(categories: List<CategoryEntity>): Transaction {
        val cat = categories.find { it.id == categoryId }?.toDomain() ?: fallbackCategory(categoryId)
        return Transaction(
            id = id,
            amount = amount,
            isExpense = isExpense,
            category = cat,
            note = note,
            date = date,
            tags = if (tags.isBlank()) emptyList() else tags.split(",")
        )
    }

    private fun CategoryEntity.toDomain(): Category = Category(
        id = id,
        name = name,
        iconKey = iconKey,
        colorHex = colorHex,
        isIncome = isIncome,
        sortOrder = sortOrder,
        isDefault = isDefault
    )

    private fun fallbackCategory(id: Long): Category = Category(
        id = id,
        name = "Unknown",
        iconKey = "other",
        colorHex = "#78909C",
        isIncome = false,
        sortOrder = 999,
        isDefault = false
    )

    private fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
        id = id,
        amount = amount,
        isExpense = isExpense,
        categoryId = category.id,
        note = note,
        date = date,
        tags = tags.joinToString(",")
    )
}