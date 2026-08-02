package com.ledger.app.domain.repository


import com.ledger.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getAll(): Flow<List<Category>>

    suspend fun getById(id: Long): Category?

    suspend fun add(
        name: String,
        iconKey: String,
        colorHex: String,
        isIncome: Boolean,
        sortOrder: Int
    ): Long

    suspend fun update(category: Category)

    suspend fun delete(category: Category)
}