package com.ledger.app.domain.usecase


import com.ledger.app.domain.model.Insight
import com.ledger.app.domain.model.InsightSeverity
import com.ledger.app.domain.repository.CategoryRepository
import com.ledger.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generates deterministic, rule-based insights from real transaction data.
 * No AI, no guessing — pure calculation.
 */
class GetInsightsUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(month: Int, year: Int): List<Insight> {
        val (start, end) = monthRange(month, year)
        val (prevStart, prevEnd) = monthRange(if (month == 1) 12 else month - 1, if (month == 1) year - 1 else year)

        val transactions = transactionRepository.getByDateRange(start, end).first()
        val prevTransactions = transactionRepository.getByDateRange(prevStart, prevEnd).first()
        val categories = categoryRepository.getAll().first()

        val insights = mutableListOf<Insight>()
        val expenses = transactions.filter { it.isExpense }
        val prevExpenses = prevTransactions.filter { it.isExpense }

        if (expenses.isEmpty()) {
            insights.add(
                Insight(
                    title = "No expenses yet",
                    description = "Start adding transactions to see insights for this month.",
                    severity = InsightSeverity.NEUTRAL
                )
            )
            return insights
        }

        // Biggest category
        val byCategory = expenses.groupBy { it.category.id }
            .mapValues { it.value.sumOf { t -> t.amount } }
        val biggestCategoryId = byCategory.maxByOrNull { it.value }?.key
        val biggestCategory = categories.find { it.id == biggestCategoryId }
        if (biggestCategory != null) {
            val amt = byCategory[biggestCategoryId] ?: 0.0
            insights.add(
                Insight(
                    title = "Biggest category: ${biggestCategory.name}",
                    description = "You've spent ${"%.0f".format(amt)} on ${biggestCategory.name} this month.",
                    severity = InsightSeverity.NEUTRAL
                )
            )
        }

        // Average daily spend
        val today = Calendar.getInstance()
        val dayOfMonth = if (month == today.get(Calendar.MONTH) + 1 && year == today.get(Calendar.YEAR)) {
            today.get(Calendar.DAY_OF_MONTH)
        } else {
            Calendar.getInstance().apply { set(year, month - 1, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        val totalExpense = expenses.sumOf { it.amount }
        val avgDaily = if (dayOfMonth > 0) totalExpense / dayOfMonth else 0.0
        insights.add(
            Insight(
                title = "Average daily spend",
                description = "You're averaging ${"%.0f".format(avgDaily)} per day this month.",
                severity = InsightSeverity.NEUTRAL
            )
        )

        // Highest spending day
        val byDay = expenses.groupBy {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(it.date))
        }.mapValues { it.value.sumOf { t -> t.amount } }
        val highestDay = byDay.maxByOrNull { it.value }
        if (highestDay != null) {
            insights.add(
                Insight(
                    title = "Highest spending day",
                    description = "${highestDay.key} was your highest spending day at ${"%.0f".format(highestDay.value)}.",
                    severity = InsightSeverity.WARNING
                )
            )
        }

        // Category growing fastest vs last month
        val prevByCategory = prevExpenses.groupBy { it.category.id }
            .mapValues { it.value.sumOf { t -> t.amount } }
        var fastestGrowthCategory: Long? = null
        var fastestGrowthPct = 0.0
        byCategory.forEach { (catId, amt) ->
            val prevAmt = prevByCategory[catId] ?: 0.0
            if (prevAmt > 0) {
                val growth = (amt - prevAmt) / prevAmt
                if (growth > fastestGrowthPct) {
                    fastestGrowthPct = growth
                    fastestGrowthCategory = catId
                }
            }
        }
        val fastestCat = categories.find { it.id == fastestGrowthCategory }
        if (fastestCat != null && fastestGrowthPct > 0) {
            insights.add(
                Insight(
                    title = "${fastestCat.name} is growing fastest",
                    description = "Up ${"%.0f".format(fastestGrowthPct * 100)}% compared to last month.",
                    severity = InsightSeverity.WARNING
                )
            )
        }

        // Compared to last month (overall)
        val prevTotal = prevExpenses.sumOf { it.amount }
        if (prevTotal > 0) {
            val diffPct = ((totalExpense - prevTotal) / prevTotal) * 100
            insights.add(
                Insight(
                    title = "Compared to last month",
                    description = if (diffPct >= 0)
                        "You've spent ${"%.0f".format(diffPct)}% more than last month."
                    else
                        "You've spent ${"%.0f".format(-diffPct)}% less than last month. Nice work.",
                    severity = if (diffPct >= 0) InsightSeverity.WARNING else InsightSeverity.POSITIVE
                )
            )
        }

        // Largest purchase
        val largest = expenses.maxByOrNull { it.amount }
        if (largest != null) {
            insights.add(
                Insight(
                    title = "Largest purchase",
                    description = "${"%.0f".format(largest.amount)} on ${largest.category.name}${if (largest.note.isNotBlank()) " — ${largest.note}" else ""}.",
                    severity = InsightSeverity.NEUTRAL
                )
            )
        }

        // No-spend days
        val spendDays = expenses.map {
            java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date))
        }.toSet()
        val noSpendDays = dayOfMonth - spendDays.size
        if (noSpendDays > 0) {
            insights.add(
                Insight(
                    title = "No-spend days",
                    description = "You had $noSpendDays day(s) this month with zero spending. Nice restraint.",
                    severity = InsightSeverity.POSITIVE
                )
            )
        }

        // Weekday vs weekend average
        val cal = Calendar.getInstance()
        val weekdayTotal = expenses.filter {
            cal.timeInMillis = it.date
            val day = cal.get(Calendar.DAY_OF_WEEK)
            day != Calendar.SATURDAY && day != Calendar.SUNDAY
        }.sumOf { it.amount }
        val weekendTotal = expenses.filter {
            cal.timeInMillis = it.date
            val day = cal.get(Calendar.DAY_OF_WEEK)
            day == Calendar.SATURDAY || day == Calendar.SUNDAY
        }.sumOf { it.amount }
        if (weekdayTotal > 0 || weekendTotal > 0) {
            insights.add(
                Insight(
                    title = if (weekendTotal > weekdayTotal) "You spend more on weekends" else "You spend more on weekdays",
                    description = "Weekday total: ${"%.0f".format(weekdayTotal)}, Weekend total: ${"%.0f".format(weekendTotal)}.",
                    severity = InsightSeverity.NEUTRAL
                )
            )
        }

        // Most frequent category (by transaction count, not amount)
        val mostFrequent = expenses.groupBy { it.category.id }
            .mapValues { it.value.size }
            .maxByOrNull { it.value }
        val frequentCat = categories.find { it.id == mostFrequent?.key }
        if (frequentCat != null) {
            insights.add(
                Insight(
                    title = "Most frequent category",
                    description = "${frequentCat.name} appears in ${mostFrequent?.value} transactions this month.",
                    severity = InsightSeverity.NEUTRAL
                )
            )
        }

        // Savings rate
        val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
        if (income > 0) {
            val savingsRate = ((income - totalExpense) / income) * 100
            insights.add(
                Insight(
                    title = "Savings rate",
                    description = if (savingsRate >= 0)
                        "You're saving ${"%.0f".format(savingsRate)}% of your income this month."
                    else
                        "You're spending ${"%.0f".format(-savingsRate)}% more than you earned this month.",
                    severity = if (savingsRate >= 20) InsightSeverity.POSITIVE else if (savingsRate < 0) InsightSeverity.NEGATIVE else InsightSeverity.NEUTRAL
                )
            )
        }
        return insights
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