package com.ledger.app.ui.dashboard


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.data.preferences.SettingsDataStore
import com.ledger.app.domain.model.DashboardSummary
import com.ledger.app.domain.usecase.GetDashboardSummaryUseCase
import com.ledger.app.ui.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary? = null,
    val currencySymbol: String = "₹",
    val month: Int = DateUtils.currentMonth(),
    val year: Int = DateUtils.currentYear()
)

class DashboardViewModel(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val monthYear = MutableStateFlow(DateUtils.currentMonth() to DateUtils.currentYear())

    val uiState: StateFlow<DashboardUiState> = monthYear
        .flatMapLatest { (month, year) ->
            combine(
                getDashboardSummaryUseCase(month, year),
                settingsDataStore.currency
            ) { summary, currency ->
                DashboardUiState(
                    isLoading = false,
                    summary = summary,
                    currencySymbol = currency,
                    month = month,
                    year = year
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    fun previousMonth() {
        val (m, y) = monthYear.value
        monthYear.value = DateUtils.previousMonth(m, y)
    }

    fun nextMonth() {
        val (m, y) = monthYear.value
        monthYear.value = DateUtils.nextMonth(m, y)
    }
}