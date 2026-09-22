package com.nuvio.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.Screen
import android.content.Intent

class NuvioCarAppService : CarAppService() {
    override fun onCreateSession(): Session {
        return object : Session() {
            override fun onCreateScreen(intent: Intent): Screen {
                return HomeScreen(carContext)
            }
        }
    }
}
