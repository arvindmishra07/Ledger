package com.ledger.app.ui.insights


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.domain.model.Insight
import com.ledger.app.domain.usecase.GetInsightsUseCase
import com.ledger.app.ui.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InsightsUiState(
    val isLoading: Boolean = true,
    val insights: List<Insight> = emptyList(),
    val month: Int = DateUtils.currentMonth(),
    val year: Int = DateUtils.currentYear()
)

class InsightsViewModel(
    private val getInsightsUseCase: GetInsightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true)
            val insights = getInsightsUseCase(state.month, state.year)
            _uiState.value = state.copy(isLoading = false, insights = insights)
        }
    }

    fun previousMonth() {
        val (m, y) = DateUtils.previousMonth(_uiState.value.month, _uiState.value.year)
        _uiState.update { it.copy(month = m, year = y) }
        load()
    }

    fun nextMonth() {
        val (m, y) = DateUtils.nextMonth(_uiState.value.month, _uiState.value.year)
        _uiState.update { it.copy(month = m, year = y) }
        load()
    }
}