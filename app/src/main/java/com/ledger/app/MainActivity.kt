package com.ledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ledger.app.ui.navigation.NavGraph
import com.ledger.app.ui.theme.LedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as LedgerApplication).container

        setContent {
            LedgerTheme {
                NavGraph(container = container)
            }
        }
    }
}