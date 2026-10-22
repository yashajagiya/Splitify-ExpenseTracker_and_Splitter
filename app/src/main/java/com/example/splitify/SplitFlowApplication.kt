package com.example.splitify

import android.app.Application
import com.example.splitify.di.AppContainer

class SplitFlowApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
