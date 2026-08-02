package com.ledger.app.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ledger.app.domain.model.Insight
import com.ledger.app.domain.model.InsightSeverity
import com.ledger.app.ui.theme.CardShape
import com.ledger.app.ui.theme.DangerRed
import com.ledger.app.ui.theme.EmeraldGreen
import com.ledger.app.ui.theme.WarningOrange

@Composable
fun InsightCard(insight: Insight, modifier: Modifier = Modifier) {
    val (icon, color) = when (insight.severity) {
        InsightSeverity.POSITIVE -> Icons.Filled.TrendingUp to EmeraldGreen
        InsightSeverity.WARNING -> Icons.Filled.WarningAmber to WarningOrange
        InsightSeverity.NEGATIVE -> Icons.Filled.TrendingDown to DangerRed
        InsightSeverity.NEUTRAL -> Icons.Filled.Lightbulb to MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(insight.title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                insight.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}