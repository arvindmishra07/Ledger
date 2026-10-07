package com.ledger.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.data.database.LedgerDatabase
import com.ledger.app.data.preferences.SettingsDataStore
import com.ledger.app.domain.repository.CategoryRepository
import com.ledger.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class SettingsUiState(
    val currency: String = "₹",
    val theme: String = "system"
)

sealed class SettingsEvent {
    data class Message(val text: String) : SettingsEvent()
    object RestartRequired : SettingsEvent()
}

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsDataStore.currency,
        settingsDataStore.theme
    ) { currency, theme -> SettingsUiState(currency, theme) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun setCurrency(value: String) {
        viewModelScope.launch { settingsDataStore.setCurrency(value) }
    }

    fun setTheme(value: String) {
        viewModelScope.launch { settingsDataStore.setTheme(value) }
    }

    fun resetAllData() {
        viewModelScope.launch {
            try {
                transactionRepository.deleteAll()
                _events.emit(SettingsEvent.Message("All transactions deleted"))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Message("Reset failed: ${e.message}"))
            }
        }
    }

    fun exportToCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAll().first()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.bufferedWriter().use { writer ->
                        writer.write("Date,Type,Category,Amount,Note,Tags\n")
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        transactions.forEach { t ->
                            val date = sdf.format(Date(t.date))
                            val type = if (t.isExpense) "Expense" else "Income"
                            val note = t.note.replace(",", ";").replace("\n", " ")
                            val tags = t.tags.joinToString(";")
                            writer.write("$date,$type,${t.category.name},${t.amount},$note,$tags\n")
                        }
                    }
                }
                _events.emit(SettingsEvent.Message("Exported ${transactions.size} transactions"))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Message("Export failed: ${e.message}"))
            }
        }
    }

    fun importFromCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val existingCategories = categoryRepository.getAll().first().toMutableList()
                var imported = 0
                var skipped = 0

                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader().useLines { lines ->
                        lines.drop(1).forEach { line ->
                            if (line.isBlank()) return@forEach
                            val parts = line.split(",")
                            if (parts.size < 4) { skipped++; return@forEach }
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val date = sdf.parse(parts[0].trim())?.time ?: System.currentTimeMillis()
                                val isExpense = parts[1].trim().equals("Expense", ignoreCase = true)
                                val categoryName = parts[2].trim()
                                val amount = parts[3].trim().toDoubleOrNull()
                                if (amount == null) { skipped++; return@forEach }
                                val note = parts.getOrNull(4)?.trim() ?: ""
                                val tags = parts.getOrNull(5)?.trim()
                                    ?.split(";")?.filter { it.isNotBlank() } ?: emptyList()

                                var category = existingCategories.find {
                                    it.name.equals(categoryName, ignoreCase = true) && it.isIncome == !isExpense
                                }
                                if (category == null) {
                                    val newId = categoryRepository.add(
                                        name = categoryName,
                                        iconKey = "other",
                                        colorHex = "#78909C",
                                        isIncome = !isExpense,
                                        sortOrder = existingCategories.size
                                    )
                                    category = categoryRepository.getById(newId)
                                    if (category != null) existingCategories.add(category)
                                }

                                if (category != null) {
                                    transactionRepository.add(
                                        amount = amount,
                                        isExpense = isExpense,
                                        categoryId = category.id,
                                        note = note,
                                        date = date,
                                        tags = tags
                                    )
                                    imported++
                                } else {
                                    skipped++
                                }
                            } catch (rowError: Exception) {
                                skipped++
                            }
                        }
                    }
                }
                _events.emit(SettingsEvent.Message("Imported $imported transactions, skipped $skipped"))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Message("Import failed: ${e.message}"))
            }
        }
    }

    fun backupDatabase(context: Context, destinationUri: Uri) {
        viewModelScope.launch {
            try {
                val dbFile = context.getDatabasePath(LedgerDatabase.DATABASE_NAME)
                context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    dbFile.inputStream().use { input -> input.copyTo(out) }
                }
                _events.emit(SettingsEvent.Message("Backup saved successfully"))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Message("Backup failed: ${e.message}"))
            }
        }
    }

    fun restoreDatabase(context: Context, sourceUri: Uri) {
        viewModelScope.launch {
            try {
                val dbFile = context.getDatabasePath(LedgerDatabase.DATABASE_NAME)
                LedgerDatabase.closeInstance()
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    dbFile.outputStream().use { out -> input.copyTo(out) }
                }
                _events.emit(SettingsEvent.RestartRequired)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Message("Restore failed: ${e.message}"))
            }
        }
    }
}