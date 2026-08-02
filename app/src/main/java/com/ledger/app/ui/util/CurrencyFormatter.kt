package com.ledger.app.ui.util


import java.text.DecimalFormat
import java.util.Locale

object CurrencyFormatter {

    private val decimalFormat = DecimalFormat("#,##,##0").apply {
        // Indian numbering style by default; falls back gracefully for other locales
    }

    fun format(amount: Double, currencySymbol: String = "₹"): String {
        val rounded = Math.round(amount)
        return "$currencySymbol${formatGrouped(rounded)}"
    }

    fun formatWithDecimals(amount: Double, currencySymbol: String = "₹"): String {
        return "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", amount)}"
    }

    fun formatCompact(amount: Double, currencySymbol: String = "₹"): String {
        return when {
            amount >= 10_000_000 -> "$currencySymbol${"%.2f".format(amount / 10_000_000)}Cr"
            amount >= 100_000 -> "$currencySymbol${"%.2f".format(amount / 100_000)}L"
            amount >= 1_000 -> "$currencySymbol${"%.1f".format(amount / 1_000)}K"
            else -> format(amount, currencySymbol)
        }
    }

    private fun formatGrouped(value: Long): String {
        // Indian digit grouping: last 3 digits, then groups of 2
        val str = value.toString()
        if (str.length <= 3) return str
        val lastThree = str.substring(str.length - 3)
        var remaining = str.substring(0, str.length - 3)
        val sb = StringBuilder()
        while (remaining.length > 2) {
            sb.insert(0, "," + remaining.substring(remaining.length - 2))
            remaining = remaining.substring(0, remaining.length - 2)
        }
        sb.insert(0, remaining)
        return sb.toString() + "," + lastThree
    }
}