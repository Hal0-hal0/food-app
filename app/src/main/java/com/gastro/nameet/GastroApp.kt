package com.gastro.nameet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.google.android.libraries.places.api.Places

@HiltAndroidApp
class GastroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        
        // Initialize Google Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        
        Timber.d("GastroApp initialized")
    }
}
