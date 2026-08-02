package com.ledger.app.ui.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.data.preferences.SettingsDataStore
import com.ledger.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currency: String = "₹",
    val theme: String = "system",
    val transactionCount: Int = 0
)

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsDataStore.currency,
        settingsDataStore.theme
    ) { currency, theme ->
        SettingsUiState(currency = currency, theme = theme, transactionCount = 0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setCurrency(value: String) {
        viewModelScope.launch { settingsDataStore.setCurrency(value) }
    }

    fun setTheme(value: String) {
        viewModelScope.launch { settingsDataStore.setTheme(value) }
    }

    fun resetAllData() {
        viewModelScope.launch { transactionRepository.deleteAll() }
    }
}