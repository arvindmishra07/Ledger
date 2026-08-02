package com.ledger.app.ui.categories


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledger.app.domain.model.Category
import com.ledger.app.ui.components.CategoryIcons
import com.ledger.app.ui.components.EmptyState
import com.ledger.app.ui.components.LedgerTopBar
import com.ledger.app.ui.theme.CardShape
import com.ledger.app.ui.theme.CategoryPalette
import androidx.compose.ui.graphics.toArgb

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LedgerTopBar(title = "Categories", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openEditor() }) {
                Icon(Icons.Filled.Add, contentDescription = "Add category")
            }
        }
    ) { padding ->
        if (state.categories.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    title = "No categories",
                    description = "Create a category to start organizing your transactions.",
                    icon = Icons.Filled.Category
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        "Expense Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(state.categories.filter { !it.isIncome }, key = { it.id }) { cat ->
                    CategoryRow(
                        category = cat,
                        onClick = { viewModel.openEditor(cat) },
                        onDelete = { viewModel.deleteCategory(cat) }
                    )
                }
                item {
                    Text(
                        "Income Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp, )
                    )
                }
                items(state.categories.filter { it.isIncome }, key = { it.id }) { cat ->
                    CategoryRow(
                        category = cat,
                        onClick = { viewModel.openEditor(cat) },
                        onDelete = { viewModel.deleteCategory(cat) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        if (state.isEditorOpen) {
            CategoryEditorSheet(
                editing = state.editingCategory,
                onDismiss = viewModel::closeEditor,
                onSave = viewModel::saveCategory
            )
        }
    }
}

@Composable
private fun CategoryRow(category: Category, onClick: () -> Unit, onDelete: () -> Unit) {
    val color = runCatching { Color(android.graphics.Color.parseColor(category.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(CategoryIcons.get(category.iconKey), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(category.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        if (!category.isDefault) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditorSheet(
    editing: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String, colorHex: String, isIncome: Boolean) -> Unit
) {
    var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var selectedIconKey by remember(editing) { mutableStateOf(editing?.iconKey ?: "other") }
    var selectedColor by remember(editing) { mutableStateOf(editing?.colorHex ?: "#00C896") }
    var isIncome by remember(editing) { mutableStateOf(editing?.isIncome ?: false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                if (editing != null) "Edit Category" else "New Category",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))
            Text("Type", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row {
                FilterChip(selected = !isIncome, onClick = { isIncome = false }, label = { Text("Expense") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = isIncome, onClick = { isIncome = true }, label = { Text("Income") })
            }

            Spacer(Modifier.height(16.dp))
            Text("Icon", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(6),
                modifier = Modifier.height(120.dp)
            ) {
                items(CategoryIcons.allIcons()) { (key, icon) ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (key == selectedIconKey) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedIconKey = key },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = key, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Color", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                CategoryPalette.take(8).forEach { color ->
                    val hex = "#" + Integer.toHexString(color.toArgb()).substring(2)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = hex }
                            .then(
                                if (selectedColor.equals(hex, ignoreCase = true))
                                    Modifier.background(color)
                                else Modifier
                            )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, selectedIconKey, selectedColor, isIncome) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Category")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}