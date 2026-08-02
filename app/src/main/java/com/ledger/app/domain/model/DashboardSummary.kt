package com.ledger.app.domain.model


data class DashboardSummary(
    val monthLabel: String,
    val totalSpent: Double,
    val totalIncome: Double,
    val overallBudget: Double,
    val remainingBudget: Double,
    val netBalance: Double,
    val savings: Double,
    val budgetProgress: Float,        // 0f..1f+ (can exceed 1 if over budget)
    val projectedEndOfMonthSpend: Double,
    val dailyAverage: Double,
    val largestExpense: Transaction?,
    val recentTransactions: List<Transaction>,
    val topCategories: List<CategorySpend>
)

data class CategorySpend(
    val category: Category,
    val total: Double,
    val percentage: Float
)