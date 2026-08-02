package com.ledger.app.data.database.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val isExpense: Boolean,
    val categoryId: Long,
    val note: String,
    val date: Long,          // epoch millis
    val tags: String,        // comma-separated tag names
    val createdAt: Long = System.currentTimeMillis()
)