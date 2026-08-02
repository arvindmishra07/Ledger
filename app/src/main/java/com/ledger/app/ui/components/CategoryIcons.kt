package com.ledger.app.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps a category's iconKey (stored in Room) to a Material icon.
 * Falls back to a generic icon for unknown/custom categories.
 */
object CategoryIcons {

    private val iconMap: Map<String, ImageVector> = mapOf(
        "food" to Icons.Filled.Restaurant,
        "groceries" to Icons.Filled.ShoppingCart,
        "travel" to Icons.Filled.Flight,
        "bills" to Icons.Filled.ReceiptLong,
        "rent" to Icons.Filled.House,
        "shopping" to Icons.Filled.ShoppingBag,
        "entertainment" to Icons.Filled.Movie,
        "investment" to Icons.Filled.TrendingUp,
        "health" to Icons.Filled.LocalHospital,
        "education" to Icons.Filled.School,
        "fuel" to Icons.Filled.LocalGasStation,
        "salary" to Icons.Filled.Payments,
        "freelance" to Icons.Filled.Work,
        "refund" to Icons.Filled.Replay,
        "gift" to Icons.Filled.CardGiftcard,
        "other" to Icons.Filled.Category
    )

    private val availableIcons: List<Pair<String, ImageVector>> = iconMap.toList()

    fun get(key: String): ImageVector = iconMap[key] ?: Icons.Filled.Category

    /** Used by the "create/edit category" icon picker. */
    fun allIcons(): List<Pair<String, ImageVector>> = availableIcons
}