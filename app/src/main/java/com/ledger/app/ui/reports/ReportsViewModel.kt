package com.ledger.app.ui.reports


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.domain.model.CategorySpend
import com.ledger.app.domain.repository.CategoryRepository
import com.ledger.app.domain.repository.TransactionRepository
import com.ledger.app.ui.util.DateUtils
import kotlinx.coroutines.flow.*

enum class ReportPeriod { WEEKLY, MONTHLY, YEARLY }

data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.MONTHLY,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val dailyTrend: List<Float> = emptyList(),
    val dailyTrendLabels: List<String> = emptyList(),
    val month: Int = DateUtils.currentMonth(),
    val year: Int = DateUtils.currentYear()
)

class ReportsViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val period = MutableStateFlow(ReportPeriod.MONTHLY)
    private val monthYear = MutableStateFlow(DateUtils.currentMonth() to DateUtils.currentYear())

    val uiState: StateFlow<ReportsUiState> = combine(
        period,
        monthYear,
        categoryRepository.getAll()
    ) { p, my, categories -> Triple(p, my, categories) }
        .flatMapLatest { (p, my, categories) ->
            val (month, year) = my
            val (start, end) = DateUtils.monthRange(month, year)

            combine(
                transactionRepository.getTotalExpense(start, end),
                transactionRepository.getTotalIncome(start, end),
                transactionRepository.getCategoryBreakdown(start, end, categories),
                transactionRepository.getByDateRange(start, end)
            ) { expense, income, breakdown, transactions ->
                val dailyTotals = transactions
                    .filter { it.isExpense }
                    .groupBy { DateUtils.formatDay(it.date) }
                    .mapValues { it.value.sumOf { t -> t.amount } }
                    .toList()
                    .sortedBy { it.first }
                    .takeLast(7)

                ReportsUiState(
                    period = p,
                    totalExpense = expense,
                    totalIncome = income,
                    categoryBreakdown = breakdown,
                    dailyTrend = dailyTotals.map { it.second.toFloat() },
                    dailyTrendLabels = dailyTotals.map { it.first },
                    month = month,
                    year = year
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())

    fun setPeriod(p: ReportPeriod) {
        period.value = p
    }

    fun previousMonth() {
        val (m, y) = monthYear.value
        monthYear.value = DateUtils.previousMonth(m, y)
    }

    fun nextMonth() {
        val (m, y) = monthYear.value
        monthYear.value = DateUtils.nextMonth(m, y)
    }
}