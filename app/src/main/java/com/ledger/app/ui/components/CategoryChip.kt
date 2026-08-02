package com.ledger.app.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ledger.app.domain.model.Category
import com.ledger.app.ui.theme.ChipShape

@Composable
fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = runCatching { Color(android.graphics.Color.parseColor(category.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    val background = if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (selected) color else Color.Transparent

    Row(
        modifier = modifier
            .clip(ChipShape)
            .background(background)
            .then(
                if (selected) Modifier
                else Modifier
            )
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}