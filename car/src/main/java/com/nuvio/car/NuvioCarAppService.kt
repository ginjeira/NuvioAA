package com.nuvio.car

import android.content.Intent
import android.util.Log
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.nuvio.app.features.addons.AddonStorage

class NuvioCarAppService : CarAppService() {

    init {
        Log.i("NuvioCar", "NuvioCarAppService: --- INIT ---")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("NuvioCar", "NuvioCarAppService: [ON_CREATE] Service created")
        try {
            AddonStorage.initialize(applicationContext)
            val existing = AddonStorage.loadInstalledAddonUrls(1)
            if (existing.isEmpty()) {
                AddonStorage.saveInstalledAddonUrls(1, listOf("https://v3-cinemeta.strem.io/manifest.json"))
                Log.i("NuvioCar", "NuvioCarAppService: Seeded default Cinemeta addon")
            }
            Log.i("NuvioCar", "NuvioCarAppService: AddonStorage initialized successfully")
        } catch (e: Throwable) {
            Log.e("NuvioCar", "NuvioCarAppService: Failed to initialize AddonStorage", e)
        }
    }

    override fun createHostValidator(): HostValidator {
        Log.i("NuvioCar", "NuvioCarAppService: createHostValidator called -> ALLOW_ALL_HOSTS_VALIDATOR granted")
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        Log.i("NuvioCar", "NuvioCarAppService: onCreateSession called -> Initializing NuvioCarSession")
        return NuvioCarSession()
    }
}

class NuvioCarSession : Session() {

    init {
        Log.i("NuvioCar", "NuvioCarSession: --- INIT ---")
    }

    override fun onCreateScreen(intent: Intent): Screen {
        Log.i("NuvioCar", "NuvioCarSession: onCreateScreen called")
        return HomeScreen(carContext)
    }
}
