package com.ledger.app.data.preferences


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ledger_settings")

class SettingsDataStore(private val context: Context) {

    private val currencyKey = stringPreferencesKey("currency")
    private val themeKey = stringPreferencesKey("theme") // "light" | "dark" | "system"

    val currency: Flow<String> = context.dataStore.data.map { it[currencyKey] ?: "₹" }
    val theme: Flow<String> = context.dataStore.data.map { it[themeKey] ?: "system" }

    suspend fun setCurrency(value: String) {
        context.dataStore.edit { it[currencyKey] = value }
    }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { it[themeKey] = value }
    }
}