package com.ledger.app.ui.budgets


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ledger.app.domain.model.Category
import com.ledger.app.ui.components.BudgetCard
import com.ledger.app.ui.components.EmptyState
import com.ledger.app.ui.components.MonthSelectorTopBar
import com.ledger.app.ui.theme.CardShape
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(viewModel: BudgetsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                MonthSelectorTopBar(
                    month = state.month,
                    year = state.year,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth
                )
            }

            item {
                Text(
                    "Overall Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(10.dp))
                if (state.overallBudget != null) {
                    BudgetCard(
                        budget = state.overallBudget!!,
                        currencySymbol = "₹",
                        onClick = viewModel::openOverallEditor,
                        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                    )
                } else {
                    SetBudgetPrompt(
                        label = "Set an overall monthly budget",
                        onClick = viewModel::openOverallEditor
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            if (state.categoryBudgets.isNotEmpty()) {
                item {
                    Text(
                        "Category Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                }
                items(state.categoryBudgets, key = { "budget_${it.id}" }) { budget ->
                    BudgetCard(
                        budget = budget,
                        currencySymbol = "₹",
                        onClick = { budget.categoryId?.let { viewModel.openCategoryEditor(it) } },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).fillMaxWidth()
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }

            if (state.categoriesWithoutBudget.isNotEmpty()) {
                item {
                    Text(
                        "Add a Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                }
                items(state.categoriesWithoutBudget, key = { "category_${it.id}" }) { cat ->
                    AddBudgetRow(category = cat, onClick = { viewModel.openCategoryEditor(cat.id) })
                }
            }

            if (state.overallBudget == null && state.categoryBudgets.isEmpty()) {
                item {
                    EmptyState(
                        title = "No budgets set",
                        description = "Set an overall budget or per-category limits to stay on track.",
                        icon = Icons.Filled.PieChart
                    )
                }
            }
        }

        if (state.isEditorOpen) {
            BudgetEditorSheet(
                categoryName = state.categoryBudgets.find { it.categoryId == state.editingCategoryId }?.categoryName
                    ?: state.categoriesWithoutBudget.find { it.id == state.editingCategoryId }?.name,
                onDismiss = viewModel::closeEditor,
                onSave = viewModel::saveBudget
            )
        }
    }
}

@Composable
private fun SetBudgetPrompt(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Icon(Icons.Filled.Add, contentDescription = "Add")
    }
}

@Composable
private fun AddBudgetRow(category: Category, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(category.name, style = MaterialTheme.typography.titleSmall)
        Icon(Icons.Filled.Add, contentDescription = "Set budget", tint = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetEditorSheet(
    categoryName: String?,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = categoryName?.let { "Budget for $it" } ?: "Overall Monthly Budget",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { amountText.toDoubleOrNull()?.let { if (it > 0) onSave(it) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Budget")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}