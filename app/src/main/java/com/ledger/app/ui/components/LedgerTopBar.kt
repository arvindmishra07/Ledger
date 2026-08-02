package com.ledger.app.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.util.DateUtils

/** Sticky top bar with a month selector (used on Dashboard, Budgets, Reports). */
@Composable
fun MonthSelectorTopBar(
    month: Int,
    year: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            text = DateUtils.formatMonthYear(month, year),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
        }
    }
}

/** Simple top bar with title and optional back button, used on secondary screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}