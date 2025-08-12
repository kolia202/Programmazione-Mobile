package com.example.myfitplan

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.osmdroid.config.Configuration

class MyFitPlanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName

        startKoin {
            androidLogger()
            androidContext(this@MyFitPlanApplication)
            modules(appModule)
        }
    }
}