package com.example.flabbygame

import android.app.Application
import com.example.flabbygame.util.SharedPrefs

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPrefs.init(this)
        instances = this
    }

    companion object {
        lateinit var instances: App
            private set
    }
}