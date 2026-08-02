package com.ledger.app.ui.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.ledger.app.ui.util.CurrencyFormatter

/**
 * Animates a currency value counting up/down whenever `amount` changes.
 */
@Composable
fun AnimatedCurrencyText(
    amount: Double,
    currencySymbol: String,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val animated by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 700),
        label = "counter"
    )

    val text = remember(animated, compact, currencySymbol) {
        if (compact) {
            CurrencyFormatter.formatCompact(animated.toDouble(), currencySymbol)
        } else {
            CurrencyFormatter.format(animated.toDouble(), currencySymbol)
        }
    }

    Text(text = text, style = style, modifier = modifier)
}