package com.ledger.app.ui.dashboard


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.*
import com.ledger.app.ui.util.CurrencyFormatter

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddTransaction: () -> Unit,
    onViewHistory: () -> Unit,
    onViewInsights: () -> Unit,
    onViewBudgets: () -> Unit,
    onViewReports: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add") },
                containerColor = EmeraldGreen,
                contentColor = androidx.compose.ui.graphics.Color(0xFF00251A)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (state.isLoading) {
                DashboardSkeleton()
            } else {
                DashboardContent(
                    state = state,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth,
                    onViewHistory = onViewHistory,
                    onViewInsights = onViewInsights,
                    onViewBudgets = onViewBudgets,
                    onViewReports = onViewReports
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onViewHistory: () -> Unit,
    onViewInsights: () -> Unit,
    onViewBudgets: () -> Unit,
    onViewReports: () -> Unit
) {
    val summary = state.summary ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            MonthSelectorTopBar(month = state.month, year = state.year, onPrevious = onPrevious, onNext = onNext)
        }

        item {
            HeroBudgetCard(
                monthLabel = summary.monthLabel,
                spent = summary.totalSpent,
                budget = summary.overallBudget,
                progress = summary.budgetProgress,
                remaining = summary.remainingBudget,
                currencySymbol = state.currencySymbol,
                onClick = onViewBudgets,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryWidget(
                    label = "Income",
                    amount = summary.totalIncome,
                    currencySymbol = state.currencySymbol,
                    accentColor = PositiveGreen,
                    modifier = Modifier.weight(1f)
                )
                SummaryWidget(
                    label = "Expense",
                    amount = summary.totalSpent,
                    currencySymbol = state.currencySymbol,
                    accentColor = DangerRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryWidget(
                    label = "Savings",
                    amount = summary.savings,
                    currencySymbol = state.currencySymbol,
                    accentColor = TealAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    label = "Daily average",
                    value = CurrencyFormatter.formatCompact(summary.dailyAverage, state.currencySymbol),
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Projected",
                    value = CurrencyFormatter.formatCompact(summary.projectedEndOfMonthSpend, state.currencySymbol),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        if (summary.topCategories.isNotEmpty()) {
            item {
                SectionHeader(title = "Top Categories", actionLabel = "Reports", onAction = onViewReports)
                Spacer(Modifier.height(10.dp))
                ChartCard(
                    title = "",
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryPieChart(
                            data = summary.topCategories,
                            modifier = Modifier.size(110.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            summary.topCategories.take(4).forEach { spend ->
                                CategoryLegendRow(
                                    name = spend.category.name,
                                    colorHex = spend.category.colorHex,
                                    percentage = spend.percentage,
                                    amount = spend.total,
                                    currencySymbol = state.currencySymbol
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        item {
            SectionHeader(title = "Recent Activity", actionLabel = "See all", onAction = onViewHistory)
            Spacer(Modifier.height(10.dp))
        }

        if (summary.recentTransactions.isEmpty()) {
            item {
                EmptyState(
                    title = "No transactions yet",
                    description = "Tap the Add button to log your first expense or income.",
                    icon = Icons.Filled.Receipt
                )
            }
        } else {
            items(summary.recentTransactions, key = { it.id }) { txn ->
                TransactionCard(
                    transaction = txn,
                    currencySymbol = state.currencySymbol,
                    onClick = onViewHistory,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .fillMaxWidth()
                )
            }
        }

        item {
            InsightsTeaser(onClick = onViewInsights, modifier = Modifier.padding(20.dp))
        }
    }
}

@Composable
private fun HeroBudgetCard(
    monthLabel: String,
    spent: Double,
    budget: Double,
    progress: Float,
    remaining: Double,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "hero_progress"
    )
    val isOver = spent > budget && budget > 0

    Column(
        modifier = modifier
            .clip(CardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Text(
            text = monthLabel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        AnimatedCurrencyText(
            amount = spent,
            currencySymbol = currencySymbol,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = if (budget > 0) "of ${CurrencyFormatter.format(budget, currencySymbol)} budget" else "No budget set",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (budget > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(50))
                        .background(if (isOver) DangerRed else EmeraldGreen)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isOver) DangerRed else EmeraldGreen
                )
                Text(
                    text = if (isOver)
                        "${CurrencyFormatter.format(-remaining, currencySymbol)} over"
                    else
                        "${CurrencyFormatter.format(remaining, currencySymbol)} left",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
private fun CategoryLegendRow(
    name: String,
    colorHex: String,
    percentage: Float,
    amount: Double,
    currencySymbol: String
) {
    val color = runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InsightsTeaser(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(EmeraldGreen.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("View your insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "See spending patterns and trends",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardSkeleton() {
    Column(modifier = Modifier.padding(20.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (it == 0) 180.dp else 70.dp)
                    .padding(vertical = 8.dp)
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}