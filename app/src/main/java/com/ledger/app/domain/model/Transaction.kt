package com.ledger.app.domain.model


data class Transaction(
    val id: Long,
    val amount: Double,
    val isExpense: Boolean,
    val category: Category,
    val note: String,
    val date: Long,
    val tags: List<String>
)