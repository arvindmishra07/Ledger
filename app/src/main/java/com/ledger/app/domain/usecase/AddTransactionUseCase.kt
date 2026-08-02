package com.ledger.app.domain.usecase


import com.ledger.app.domain.repository.TransactionRepository

sealed class AddTransactionResult {
    data class Success(val id: Long) : AddTransactionResult()
    data class Error(val message: String) : AddTransactionResult()
}

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        amount: Double,
        isExpense: Boolean,
        categoryId: Long?,
        note: String,
        date: Long,
        tags: List<String>
    ): AddTransactionResult {
        if (amount <= 0.0) {
            return AddTransactionResult.Error("Amount must be greater than zero")
        }
        if (categoryId == null) {
            return AddTransactionResult.Error("Please select a category")
        }

        val id = transactionRepository.add(
            amount = amount,
            isExpense = isExpense,
            categoryId = categoryId,
            note = note.trim(),
            date = date,
            tags = tags
        )
        return AddTransactionResult.Success(id)
    }
}