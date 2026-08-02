package com.ledger.app.ui.addtransaction


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.repository.CategoryRepository
import com.ledger.app.domain.repository.TransactionRepository
import com.ledger.app.domain.usecase.AddTransactionResult
import com.ledger.app.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddTransactionUiState(
    val isExpense: Boolean = true,
    val amountText: String = "",
    val selectedCategory: Category? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false
)

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    private val editingTransactionId: Long?
) : ViewModel() {

    private val addTransactionUseCase = AddTransactionUseCase(transactionRepository)

    private val _uiState = MutableStateFlow(AddTransactionUiState(isEditing = editingTransactionId != null))
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAll().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        if (editingTransactionId != null) {
            loadExistingTransaction(editingTransactionId)
        }
    }

    private fun loadExistingTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.getAll().collect { all ->
                val txn = all.find { it.id == id } ?: return@collect
                _uiState.update {
                    it.copy(
                        isExpense = txn.isExpense,
                        amountText = txn.amount.toInt().toString(),
                        selectedCategory = txn.category,
                        note = txn.note,
                        date = txn.date,
                        tags = txn.tags
                    )
                }
            }
        }
    }

    fun setIsExpense(isExpense: Boolean) {
        _uiState.update { it.copy(isExpense = isExpense, selectedCategory = null) }
    }

    fun setAmountText(text: String) {
        // Allow only digits and a single decimal point
        val filtered = text.filterIndexed { index, c ->
            c.isDigit() || (c == '.' && text.indexOf('.') == index)
        }
        _uiState.update { it.copy(amountText = filtered, errorMessage = null) }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun setDate(millis: Long) {
        _uiState.update { it.copy(date = millis) }
    }

    fun addTag(tag: String) {
        if (tag.isBlank()) return
        _uiState.update { it.copy(tags = it.tags + tag.trim()) }
    }

    fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags - tag) }
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amountText.toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Enter a valid amount") }
            return
        }
        if (state.selectedCategory == null) {
            _uiState.update { it.copy(errorMessage = "Select a category") }
            return
        }

        viewModelScope.launch {
            if (state.isEditing && editingTransactionId != null) {
                transactionRepository.update(
                    com.ledger.app.domain.model.Transaction(
                        id = editingTransactionId,
                        amount = amount,
                        isExpense = state.isExpense,
                        category = state.selectedCategory,
                        note = state.note.trim(),
                        date = state.date,
                        tags = state.tags
                    )
                )
                _uiState.update { it.copy(isSaved = true) }
            } else {
                val result = addTransactionUseCase(
                    amount = amount,
                    isExpense = state.isExpense,
                    categoryId = state.selectedCategory.id,
                    note = state.note,
                    date = state.date,
                    tags = state.tags
                )
                when (result) {
                    is AddTransactionResult.Success -> _uiState.update { it.copy(isSaved = true) }
                    is AddTransactionResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }
}