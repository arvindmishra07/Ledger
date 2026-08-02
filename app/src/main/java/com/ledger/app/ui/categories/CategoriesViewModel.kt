package com.ledger.app.ui.categories


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isEditorOpen: Boolean = false,
    val editingCategory: Category? = null
)

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val editorState = MutableStateFlow<Pair<Boolean, Category?>>(false to null)

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.getAll(),
        editorState
    ) { categories, (isOpen, editing) ->
        CategoriesUiState(
            categories = categories,
            isEditorOpen = isOpen,
            editingCategory = editing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoriesUiState())

    fun openEditor(category: Category? = null) {
        editorState.value = true to category
    }

    fun closeEditor() {
        editorState.value = false to null
    }

    fun saveCategory(
        name: String,
        iconKey: String,
        colorHex: String,
        isIncome: Boolean
    ) {
        viewModelScope.launch {
            val editing = editorState.value.second
            if (editing != null) {
                categoryRepository.update(
                    editing.copy(name = name, iconKey = iconKey, colorHex = colorHex, isIncome = isIncome)
                )
            } else {
                val nextOrder = uiState.value.categories.size
                categoryRepository.add(
                    name = name,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    isIncome = isIncome,
                    sortOrder = nextOrder
                )
            }
            closeEditor()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.delete(category)
        }
    }

    fun reorder(from: Int, to: Int) {
        viewModelScope.launch {
            val list = uiState.value.categories.toMutableList()
            if (from !in list.indices || to !in list.indices) return@launch
            val item = list.removeAt(from)
            list.add(to, item)
            list.forEachIndexed { index, cat ->
                if (cat.sortOrder != index) {
                    categoryRepository.update(cat.copy(sortOrder = index))
                }
            }
        }
    }
}