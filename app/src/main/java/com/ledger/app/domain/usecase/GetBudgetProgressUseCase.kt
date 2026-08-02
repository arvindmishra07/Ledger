package com.ledger.app.domain.usecase


import com.ledger.app.domain.model.Budget
import com.ledger.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class GetBudgetProgressUseCase(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(month: Int, year: Int): Flow<List<Budget>> {
        return budgetRepository.getForMonth(month, year)
    }
}