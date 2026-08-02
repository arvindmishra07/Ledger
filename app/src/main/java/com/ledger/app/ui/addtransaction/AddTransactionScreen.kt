package com.ledger.app.ui.addtransaction


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.components.CategoryChip
import androidx.compose.ui.composed
import com.ledger.app.ui.theme.DangerRed
import com.ledger.app.ui.theme.EmeraldGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::save) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Expense / Income toggle
            ExpenseIncomeToggle(
                isExpense = state.isExpense,
                onToggle = viewModel::setIsExpense
            )

            Spacer(Modifier.height(24.dp))

            // Amount input
            AmountInput(
                amountText = state.amountText,
                isExpense = state.isExpense,
                onAmountChange = viewModel::setAmountText,
                focusRequester = focusRequester
            )

            Spacer(Modifier.height(28.dp))

            Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            val filteredCategories = state.categories.filter { it.isIncome != state.isExpense }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredCategories, key = { it.id }) { category ->
                    CategoryChip(
                        category = category,
                        selected = state.selectedCategory?.id == category.id,
                        onClick = { viewModel.selectCategory(category) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Note", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                placeholder = { Text("Add a note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            AnimatedVisibility(visible = state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "",
                    color = DangerRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isExpense) DangerRed else EmeraldGreen
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isEditing) "Update Transaction" else "Save Transaction", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExpenseIncomeToggle(isExpense: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        ToggleSegment(
            label = "Expense",
            selected = isExpense,
            color = DangerRed,
            onClick = { onToggle(true) },
            modifier = Modifier.weight(1f)
        )
        ToggleSegment(
            label = "Income",
            selected = !isExpense,
            color = EmeraldGreen,
            onClick = { onToggle(false) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ToggleSegment(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.96f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "segment_scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.18f) else Color.Transparent)
            .clip(RoundedCornerShape(10.dp))
            .then(Modifier)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            onClick = onClick
        )
    )
}

@Composable
private fun AmountInput(
    amountText: String,
    isExpense: Boolean,
    onAmountChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "₹",
                style = MaterialTheme.typography.displayMedium,
                color = if (isExpense) DangerRed else EmeraldGreen
            )
            BasicAmountField(
                value = amountText,
                onValueChange = onAmountChange,
                focusRequester = focusRequester,
                color = if (isExpense) DangerRed else EmeraldGreen
            )
        }
    }
}

@Composable
private fun BasicAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    color: Color
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.displayMedium.copy(color = color),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
            .focusRequester(focusRequester)
            .widthIn(min = 60.dp),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
            innerTextField()
        }
    )
}