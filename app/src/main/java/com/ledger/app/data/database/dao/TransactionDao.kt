package com.ledger.app.data.database.dao


import androidx.room.*
import com.ledger.app.data.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :start AND :end 
        ORDER BY date DESC
    """)
    fun getByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :start AND :end 
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:isExpense IS NULL OR isExpense = :isExpense)
        ORDER BY 
        CASE WHEN :sortBy = 'date_desc' THEN date END DESC,
        CASE WHEN :sortBy = 'date_asc' THEN date END ASC,
        CASE WHEN :sortBy = 'amount_desc' THEN amount END DESC,
        CASE WHEN :sortBy = 'amount_asc' THEN amount END ASC
    """)
    fun getFiltered(
        start: Long,
        end: Long,
        categoryId: Long?,
        isExpense: Boolean?,
        sortBy: String
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE note LIKE '%' || :query || '%' 
        OR tags LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    fun search(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isExpense = 1 AND date BETWEEN :start AND :end")
    fun getTotalExpense(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isExpense = 0 AND date BETWEEN :start AND :end")
    fun getTotalIncome(start: Long, end: Long): Flow<Double>

    @Query("""
        SELECT categoryId, SUM(amount) as total FROM transactions 
        WHERE isExpense = 1 AND date BETWEEN :start AND :end 
        GROUP BY categoryId ORDER BY total DESC
    """)
    fun getCategoryTotals(start: Long, end: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY amount DESC LIMIT 1")
    suspend fun getLargestExpense(start: Long, end: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

data class CategoryTotal(
    val categoryId: Long,
    val total: Double
)