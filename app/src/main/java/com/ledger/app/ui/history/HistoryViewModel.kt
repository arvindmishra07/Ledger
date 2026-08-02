package com.ledger.app.ui.history


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.Transaction
import com.ledger.app.domain.repository.CategoryRepository
import com.ledger.app.domain.repository.TransactionRepository
import com.ledger.app.ui.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption(val key: String, val label: String) {
    DATE_DESC("date_desc", "Newest"),
    DATE_ASC("date_asc", "Oldest"),
    AMOUNT_DESC("amount_desc", "Highest"),
    AMOUNT_ASC("amount_asc", "Lowest")
}

data class HistoryFilters(
    val month: Int = DateUtils.currentMonth(),
    val year: Int = DateUtils.currentYear(),
    val categoryId: Long? = null,
    val isExpense: Boolean? = null,
    val sort: SortOption = SortOption.DATE_DESC,
    val searchQuery: String = ""
)

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val filters: HistoryFilters = HistoryFilters(),
    val selectedIds: Set<Long> = emptySet(),
    val recentlyDeleted: Transaction? = null
)

class HistoryViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val filters = MutableStateFlow(HistoryFilters())
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val recentlyDeleted = MutableStateFlow<Transaction?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        filters,
        categoryRepository.getAll(),
        selectedIds,
        recentlyDeleted
    ) { f, categories, selected, deleted ->
        Triple(f, categories, selected) to deleted
    }.flatMapLatest { (triple, deleted) ->
        val (f, categories, selected) = triple
        val txnFlow = if (f.searchQuery.isNotBlank()) {
            transactionRepository.search(f.searchQuery)
        } else {
            val (start, end) = DateUtils.monthRange(f.month, f.year)
            transactionRepository.getFiltered(start, end, f.categoryId, f.isExpense, f.sort.key)
        }
        txnFlow.map { txns ->
            HistoryUiState(
                transactions = txns,
                categories = categories,
                filters = f,
                selectedIds = selected,
                recentlyDeleted = deleted
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun setSearchQuery(query: String) {
        filters.update { it.copy(searchQuery = query) }
    }

    fun setMonth(month: Int, year: Int) {
        filters.update { it.copy(month = month, year = year) }
    }

    fun setCategoryFilter(categoryId: Long?) {
        filters.update { it.copy(categoryId = categoryId) }
    }

    fun setTypeFilter(isExpense: Boolean?) {
        filters.update { it.copy(isExpense = isExpense) }
    }

    fun setSort(sort: SortOption) {
        filters.update { it.copy(sort = sort) }
    }

    fun toggleSelection(id: Long) {
        selectedIds.update { if (id in it) it - id else it + id }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
            recentlyDeleted.value = transaction
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val toDelete = uiState.value.transactions.filter { it.id in selectedIds.value }
            toDelete.forEach { transactionRepository.delete(it) }
            selectedIds.value = emptySet()
        }
    }

    fun undoDelete() {
        val txn = recentlyDeleted.value ?: return
        viewModelScope.launch {
            transactionRepository.add(
                amount = txn.amount,
                isExpense = txn.isExpense,
                categoryId = txn.category.id,
                note = txn.note,
                date = txn.date,
                tags = txn.tags
            )
            recentlyDeleted.value = null
        }
    }

    fun dismissUndo() {
        recentlyDeleted.value = null
    }

    fun duplicateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.add(
                amount = transaction.amount,
                isExpense = transaction.isExpense,
                categoryId = transaction.category.id,
                note = transaction.note,
                date = System.currentTimeMillis(),
                tags = transaction.tags
            )
        }
    }
}