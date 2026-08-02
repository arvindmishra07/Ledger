package com.ledger.app.domain.model


data class Category(
    val id: Long,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val isIncome: Boolean,
    val sortOrder: Int,
    val isDefault: Boolean
)