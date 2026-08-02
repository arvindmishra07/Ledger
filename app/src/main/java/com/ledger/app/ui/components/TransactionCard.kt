package com.ledger.app.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledger.app.domain.model.Transaction
import com.ledger.app.ui.theme.CardShape
import com.ledger.app.ui.theme.DangerRed
import com.ledger.app.ui.theme.PositiveGreen
import com.ledger.app.ui.util.CurrencyFormatter
import com.ledger.app.ui.util.DateUtils

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionCard(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val categoryColor = runCatching { Color(android.graphics.Color.parseColor(transaction.category.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = CategoryIcons.get(transaction.category.iconKey),
                contentDescription = transaction.category.name,
                tint = categoryColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.category.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            val subtitle = buildString {
                append(DateUtils.relativeDayLabel(transaction.date))
                if (transaction.note.isNotBlank()) {
                    append(" · ")
                    append(transaction.note)
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Text(
            text = (if (transaction.isExpense) "-" else "+") + CurrencyFormatter.format(transaction.amount, currencySymbol),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.isExpense) DangerRed else PositiveGreen
        )
    }
}