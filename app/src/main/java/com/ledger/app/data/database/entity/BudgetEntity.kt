package com.ledger.app.data.database.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long?,   // null = overall monthly budget
    val amount: Double,
    val month: Int,          // 1-12
    val year: Int
)