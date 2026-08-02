package com.ledger.app.domain.usecase


import com.ledger.app.domain.model.CategorySpend
import com.ledger.app.domain.model.DashboardSummary
import com.ledger.app.domain.repository.BudgetRepository
import com.ledger.app.domain.repository.CategoryRepository
import com.ledger.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class GetDashboardSummaryUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) {

    operator fun invoke(month: Int, year: Int): Flow<DashboardSummary> {
        val (start, end) = monthRange(month, year)

        val categoriesFlow = categoryRepository.getAll()
        val expenseFlow = transactionRepository.getTotalExpense(start, end)
        val incomeFlow = transactionRepository.getTotalIncome(start, end)
        val recentFlow = transactionRepository.getByDateRange(start, end)

        return combine(
            categoriesFlow,
            expenseFlow,
            incomeFlow,
            recentFlow
        ) { categories, expense, income, recent ->

            val overallBudget = budgetRepository.getOverallBudgetAmount(month, year) ?: 0.0
            val remaining = overallBudget - expense
            val progress = if (overallBudget <= 0) 0f else (expense / overallBudget).toFloat()

            val today = Calendar.getInstance()
            val dayOfMonth = if (month == today.get(Calendar.MONTH) + 1 && year == today.get(Calendar.YEAR)) {
                today.get(Calendar.DAY_OF_MONTH)
            } else {
                Calendar.getInstance().apply { set(year, month - 1, 1) }
                    .getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            val daysInMonth = Calendar.getInstance().apply { set(year, month - 1, 1) }
                .getActualMaximum(Calendar.DAY_OF_MONTH)

            val dailyAverage = if (dayOfMonth > 0) expense / dayOfMonth else 0.0
            val projected = dailyAverage * daysInMonth

            val largest = transactionRepository.getLargestExpense(start, end)

            val categoryTotals = mutableMapOf<Long, Double>()
            recent.filter { it.isExpense }.forEach { t ->
                categoryTotals[t.category.id] = (categoryTotals[t.category.id] ?: 0.0) + t.amount
            }
            val topCategories = categoryTotals.entries
                .sortedByDescending { it.value }
                .take(5)
                .mapNotNull { entry ->
                    categories.find { it.id == entry.key }?.let { cat ->
                        CategorySpend(
                            category = cat,
                            total = entry.value,
                            percentage = if (expense > 0) (entry.value / expense).toFloat() else 0f
                        )
                    }
                }

            DashboardSummary(
                monthLabel = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(start)),
                totalSpent = expense,
                totalIncome = income,
                overallBudget = overallBudget,
                remainingBudget = remaining,
                netBalance = income - expense,
                savings = (income - expense).coerceAtLeast(0.0),
                budgetProgress = progress,
                projectedEndOfMonthSpend = projected,
                dailyAverage = dailyAverage,
                largestExpense = largest,
                recentTransactions = recent.take(5),
                topCategories = topCategories
            )
        }
    }

    private fun monthRange(month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return start to end
    }
}