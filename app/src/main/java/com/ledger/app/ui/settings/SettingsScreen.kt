package com.ledger.app.ui.settings


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onManageCategories: () -> Unit,
    onManageBudgets: () -> Unit,
    onViewReports: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            item {
                SettingsSectionHeader("General")
                SettingsRow(icon = Icons.Filled.CurrencyRupee, title = "Currency", subtitle = state.currency, onClick = { showCurrencyDialog = true })
                SettingsRow(icon = Icons.Filled.DarkMode, title = "Theme", subtitle = state.theme.replaceFirstChar { it.uppercase() }, onClick = { showThemeDialog = true })
            }
            item {
                SettingsSectionHeader("Manage")
                SettingsRow(icon = Icons.Filled.Category, title = "Manage Categories", onClick = onManageCategories)
                SettingsRow(icon = Icons.Filled.PieChart, title = "Manage Budgets", onClick = onManageBudgets)
                SettingsRow(icon = Icons.Filled.BarChart, title = "Reports", onClick = onViewReports)
            }
            item {
                SettingsSectionHeader("Data")
                SettingsRow(icon = Icons.Filled.Upload, title = "Export Data", subtitle = "CSV / JSON", onClick = {})
                SettingsRow(icon = Icons.Filled.Download, title = "Import Data", onClick = {})
                SettingsRow(icon = Icons.Filled.Backup, title = "Backup", onClick = {})
                SettingsRow(icon = Icons.Filled.Restore, title = "Restore", onClick = {})
                SettingsRow(
                    icon = Icons.Filled.DeleteForever,
                    title = "Reset Data",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showResetDialog = true }
                )
            }
            item {
                SettingsSectionHeader("About")
                SettingsRow(icon = Icons.Filled.Info, title = "About Ledger", subtitle = "Version 1.0", onClick = {})
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all data?") },
            text = { Text("This will permanently delete all transactions. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAllData()
                    showResetDialog = false
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCurrencyDialog) {
        val options = listOf("₹", "$", "€", "£", "¥")
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    options.forEach { symbol ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.currency == symbol,
                                onClick = {
                                    viewModel.setCurrency(symbol)
                                    showCurrencyDialog = false
                                }
                            )
                            Text(symbol, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            }
        )
    }

    if (showThemeDialog) {
        val options = listOf("light", "dark", "system")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    options.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.theme == theme,
                                onClick = {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                            )
                            Text(theme.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable_settings(onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Modifier.clickable_settings(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(onClick = onClick)
)