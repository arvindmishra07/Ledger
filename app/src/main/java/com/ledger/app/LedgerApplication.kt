package com.ledger.app


import android.app.Application
import com.ledger.app.di.AppContainer

class LedgerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}