package com.ledger.app.data.database.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,     // maps to an icon in CategoryIcons
    val colorHex: String,
    val isIncome: Boolean = false,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false
)