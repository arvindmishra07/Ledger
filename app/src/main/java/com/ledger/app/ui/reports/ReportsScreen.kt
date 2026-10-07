package com.ledger.app.ui.reports


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.components.BarChart
import com.ledger.app.ui.components.CategoryPieChart
import com.ledger.app.ui.components.ChartCard
import com.ledger.app.ui.components.MonthSelectorTopBar
import com.ledger.app.ui.theme.DangerRed
import com.ledger.app.ui.theme.PositiveGreen
import com.ledger.app.ui.util.CurrencyFormatter
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip

@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportStat(
                        label = "Income",
                        value = CurrencyFormatter.formatCompact(state.totalIncome),
                        color = PositiveGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStat(
                        label = "Expense",
                        value = CurrencyFormatter.formatCompact(state.totalExpense),
                        color = DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
            item {
                ChartCard(
                    title = "Income vs Expense",
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                ) {
                    BarChart(
                        values = listOf(state.totalIncome.toFloat(), state.totalExpense.toFloat()),
                        labels = listOf("Income", "Expense")
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
            if (state.dailyTrend.isNotEmpty()) {
                item {
                    ChartCard(
                        title = "Daily Trend",
                        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                    ) {
                        BarChart(values = state.dailyTrend, labels = state.dailyTrendLabels)
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }

            if (state.categoryBreakdown.isNotEmpty()) {
                item {
                    ChartCard(
                        title = "Category Breakdown",
                        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryPieChart(
                                data = state.categoryBreakdown,
                                modifier = Modifier.size(120.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                state.categoryBreakdown.forEach { spend ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(spend.category.name, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            CurrencyFormatter.formatCompact(spend.total),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStat(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
    }
}