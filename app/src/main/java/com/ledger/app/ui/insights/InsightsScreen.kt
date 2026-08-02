package com.ledger.app.ui.insights


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.components.EmptyState
import com.ledger.app.ui.components.InsightCard
import com.ledger.app.ui.components.MonthSelectorTopBar

@Composable
fun InsightsScreen(viewModel: InsightsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { padding ->
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

            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.insights.isEmpty()) {
                item {
                    EmptyState(
                        title = "No insights yet",
                        description = "Add some transactions this month to see insights.",
                        icon = Icons.Filled.Lightbulb
                    )
                }
            } else {
                items(state.insights) { insight ->
                    InsightCard(
                        insight = insight,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth()
                    )
                }
            }
        }
    }
}