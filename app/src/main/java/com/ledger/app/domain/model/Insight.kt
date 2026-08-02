package com.ledger.app.domain.model


enum class InsightSeverity { POSITIVE, NEUTRAL, WARNING, NEGATIVE }

data class Insight(
    val title: String,
    val description: String,
    val severity: InsightSeverity
)