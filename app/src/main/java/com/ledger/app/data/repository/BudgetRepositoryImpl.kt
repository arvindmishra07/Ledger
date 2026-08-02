package com.ledger.app.data.repository


import com.ledger.app.data.database.dao.BudgetDao
import com.ledger.app.data.database.dao.CategoryDao
import com.ledger.app.data.database.entity.BudgetEntity
import com.ledger.app.domain.model.Budget
import com.ledger.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getForMonth(month: Int, year: Int): Flow<List<Budget>> {
        return budgetDao.getForMonth(month, year).map { list ->
            list.map { entity ->
                Budget(
                    id = entity.id,
                    categoryId = entity.categoryId,
                    categoryName = null, // resolved in ViewModel with category list if needed
                    amount = entity.amount,
                    spent = 0.0, // spent is computed in ViewModel by combining with transaction totals
                    month = entity.month,
                    year = entity.year
                )
            }
        }
    }

    override suspend fun getOverallBudgetAmount(month: Int, year: Int): Double? {
        return budgetDao.getOverallBudget(month, year)?.amount
    }

    override suspend fun setOverallBudget(amount: Double, month: Int, year: Int) {
        val existing = budgetDao.getOverallBudget(month, year)
        if (existing != null) {
            budgetDao.update(existing.copy(amount = amount))
        } else {
            budgetDao.insert(BudgetEntity(categoryId = null, amount = amount, month = month, year = year))
        }
    }

    override suspend fun setCategoryBudget(categoryId: Long, amount: Double, month: Int, year: Int) {
        val existing = budgetDao.getCategoryBudget(categoryId, month, year)
        if (existing != null) {
            budgetDao.update(existing.copy(amount = amount))
        } else {
            budgetDao.insert(BudgetEntity(categoryId = categoryId, amount = amount, month = month, year = year))
        }
    }

    override suspend fun delete(budgetId: Long) {
        budgetDao.deleteById(budgetId)
    }
}