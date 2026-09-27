package com.mortex.amromovies

import android.app.Application
import com.mortex.amromovies.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AmroMoviesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AmroMoviesApplication)
            modules(appModules)
        }
    }
}
