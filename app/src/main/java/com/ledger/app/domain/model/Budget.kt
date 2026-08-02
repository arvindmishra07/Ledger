package com.ledger.app.domain.model


data class Budget(
    val id: Long,
    val categoryId: Long?,     // null = overall budget
    val categoryName: String?, // null for overall
    val amount: Double,
    val spent: Double,
    val month: Int,
    val year: Int
) {
    val remaining: Double get() = amount - spent
    val progress: Float get() = if (amount <= 0) 0f else (spent / amount).toFloat().coerceIn(0f, 1.5f)
    val isExceeded: Boolean get() = spent > amount
    val isWarning: Boolean get() = !isExceeded && progress >= 0.8f
}