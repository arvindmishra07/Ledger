package com.ledger.app.data.database.dao


import androidx.room.*
import com.ledger.app.data.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getForMonth(month: Int, year: Int): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId IS NULL AND month = :month AND year = :year LIMIT 1")
    suspend fun getOverallBudget(month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month AND year = :year LIMIT 1")
    suspend fun getCategoryBudget(categoryId: Long, month: Int, year: Int): BudgetEntity?

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}