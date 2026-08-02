package com.ledger.app.ui.theme


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val LedgerShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Additional custom shapes used across components
val CardShape = RoundedCornerShape(20.dp)
val ChipShape = RoundedCornerShape(50)
val BottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val FabShape = RoundedCornerShape(18.dp)