package com.ledger.app.ui.history


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.components.CategoryChip
import com.ledger.app.ui.components.EmptyState
import com.ledger.app.ui.components.TransactionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onTransactionClick: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.recentlyDeleted) {
        if (state.recentlyDeleted != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Transaction deleted",
                actionLabel = "Undo",
                withDismissAction = true
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.dismissUndo()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filters")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            OutlinedTextField(
                value = state.filters.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.filters.isExpense == null,
                        onClick = { viewModel.setTypeFilter(null) },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.filters.isExpense == true,
                        onClick = { viewModel.setTypeFilter(true) },
                        label = { Text("Expense") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.filters.isExpense == false,
                        onClick = { viewModel.setTypeFilter(false) },
                        label = { Text("Income") }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(visible = state.selectedIds.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${state.selectedIds.size} selected", style = MaterialTheme.typography.titleSmall)
                    Row {
                        TextButton(onClick = viewModel::clearSelection) { Text("Cancel") }
                        TextButton(onClick = viewModel::deleteSelected) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (state.transactions.isEmpty()) {
                EmptyState(
                    title = "No transactions found",
                    description = "Try adjusting your filters or search terms.",
                    icon = Icons.Filled.Receipt
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(state.transactions, key = { it.id }) { txn ->
                        val selected = txn.id in state.selectedIds
                        TransactionCard(
                            transaction = txn,
                            currencySymbol = "₹",
                            selected = selected,
                            onClick = {
                                if (state.selectedIds.isNotEmpty()) {
                                    viewModel.toggleSelection(txn.id)
                                } else {
                                    onTransactionClick(txn.id)
                                }
                            },
                            onLongClick = { viewModel.toggleSelection(txn.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        if (showFilterSheet) {
            FilterSheet(
                categories = state.categories,
                selectedCategoryId = state.filters.categoryId,
                selectedSort = state.filters.sort,
                onCategorySelect = viewModel::setCategoryFilter,
                onSortSelect = viewModel::setSort,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    categories: List<com.ledger.app.domain.model.Category>,
    selectedCategoryId: Long?,
    selectedSort: SortOption,
    onCategorySelect: (Long?) -> Unit,
    onSortSelect: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Sort by", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SortOption.values()) { option ->
                    FilterChip(
                        selected = selectedSort == option,
                        onClick = { onSortSelect(option) },
                        label = { Text(option.label) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text("All") }
                    )
                }
                items(categories) { cat ->
                    CategoryChip(
                        category = cat,
                        selected = selectedCategoryId == cat.id,
                        onClick = { onCategorySelect(cat.id) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}