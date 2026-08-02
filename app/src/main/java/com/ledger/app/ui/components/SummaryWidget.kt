package com.ledger.app.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.theme.CardShape
import com.ledger.app.ui.util.CurrencyFormatter

/** Small stat card used in a row (Income / Expense / Savings on Dashboard). */
@Composable
fun SummaryWidget(
    label: String,
    amount: Double,
    currencySymbol: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(accentColor)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = CurrencyFormatter.formatCompact(amount, currencySymbol),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}