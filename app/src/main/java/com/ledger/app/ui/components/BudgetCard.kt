package com.ledger.app.ui.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledger.app.domain.model.Budget
import com.ledger.app.ui.theme.CardShape
import com.ledger.app.ui.theme.DangerRed
import com.ledger.app.ui.theme.EmeraldGreen
import com.ledger.app.ui.theme.WarningOrange
import com.ledger.app.ui.util.CurrencyFormatter

@Composable
fun BudgetCard(
    budget: Budget,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressColor = when {
        budget.isExceeded -> DangerRed
        budget.isWarning -> WarningOrange
        else -> EmeraldGreen
    }

    val animatedProgress by animateFloatAsState(
        targetValue = budget.progress.coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "budget_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = budget.categoryName ?: "Overall Budget",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            if (budget.isExceeded) {
                Text(
                    text = "Over budget",
                    style = MaterialTheme.typography.labelMedium,
                    color = DangerRed
                )
            } else if (budget.isWarning) {
                Text(
                    text = "Near limit",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarningOrange
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(progressColor)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${CurrencyFormatter.format(budget.spent, currencySymbol)} spent",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${CurrencyFormatter.format(budget.amount, currencySymbol)} budget",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}