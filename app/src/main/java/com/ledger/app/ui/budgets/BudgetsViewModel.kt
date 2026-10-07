package com.ledger.app.ui.budgets


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.domain.model.Budget
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.repository.BudgetRepository
import com.ledger.app.domain.repository.CategoryRepository
import com.ledger.app.domain.repository.TransactionRepository
import com.ledger.app.ui.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BudgetsUiState(
    val month: Int = DateUtils.currentMonth(),
    val year: Int = DateUtils.currentYear(),
    val overallBudget: Budget? = null,
    val categoryBudgets: List<Budget> = emptyList(),
    val categoriesWithoutBudget: List<Category> = emptyList(),
    val isEditorOpen: Boolean = false,
    val editingCategoryId: Long? = null // null means editing overall budget
)

class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val events: kotlinx.coroutines.flow.SharedFlow<String> = _events.asSharedFlow()
    private val monthYear = MutableStateFlow(DateUtils.currentMonth() to DateUtils.currentYear())
    private val editorState = MutableStateFlow<Pair<Boolean, Long?>>(false to null)

    val uiState: StateFlow<BudgetsUiState> = monthYear
        .flatMapLatest { (month, year) ->
            val (start, end) = DateUtils.monthRange(month, year)
            val categoriesFlow = categoryRepository.getAll()

            categoriesFlow.flatMapLatest { categories ->
                combine(
                    budgetRepository.getForMonth(month, year),
                    transactionRepository.getCategoryBreakdown(start, end, categories),
                    transactionRepository.getTotalExpense(start, end),
                    editorState
                ) { budgets, breakdown, totalExpense, editor ->
                    val spendMap = breakdown.associate { it.category.id to it.total }

                    val overall = budgets.find { it.categoryId == null }
                    val perCategory = budgets.filter { it.categoryId != null }
                    val categoriesWithBudget = perCategory.mapNotNull { it.categoryId }.toSet()
                    val remainingCategories = categories.filter {
                        !it.isIncome && it.id !in categoriesWithBudget
                    }

                    BudgetsUiState(
                        month = month,
                        year = year,
                        overallBudget = overall?.copy(spent = totalExpense),
                        categoryBudgets = perCategory.map { b ->
                            val cat = categories.find { it.id == b.categoryId }
                            b.copy(
                                categoryName = cat?.name ?: "Unknown",
                                spent = spendMap[b.categoryId] ?: 0.0
                            )
                        },
                        categoriesWithoutBudget = remainingCategories,
                        isEditorOpen = editor.first,
                        editingCategoryId = editor.second
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetsUiState())
    fun previousMonth() {
        val (m, y) = monthYear.value
        monthYear.value = DateUtils.previousMonth(m, y)
    }

    fun nextMonth() {
        val (m, y) = monthYear.value
        monthYear.value = DateUtils.nextMonth(m, y)
    }

    fun openOverallEditor() {
        editorState.value = true to null
    }

    fun openCategoryEditor(categoryId: Long) {
        editorState.value = true to categoryId
    }

    fun closeEditor() {
        editorState.value = false to null
    }

    fun saveBudget(amount: Double) {
        if (amount <= 0.0) {
            viewModelScope.launch { _events.emit("Enter a valid budget amount") }
            return
        }
        viewModelScope.launch {
            try {
                val (month, year) = monthYear.value
                val categoryId = editorState.value.second
                if (categoryId == null) {
                    budgetRepository.setOverallBudget(amount, month, year)
                } else {
                    budgetRepository.setCategoryBudget(categoryId, amount, month, year)
                }
                closeEditor()
            } catch (e: Exception) {
                _events.emit("Couldn't save budget: ${e.message}")
            }
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            budgetRepository.delete(budgetId)
        }
    }
}