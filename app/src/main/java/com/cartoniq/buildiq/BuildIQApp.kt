package com.cartoniq.buildiq

import android.app.Application
import com.cartoniq.buildiq.data.AppDatabase

class BuildIQApp : Application() {
    val database by lazy { AppDatabase.get(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
