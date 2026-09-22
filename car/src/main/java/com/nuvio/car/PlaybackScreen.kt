package com.nuvio.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template

class PlaybackScreen(
    carContext: CarContext
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        return MessageTemplate.Builder(
            "Playback ainda não implementado"
        )
            .setTitle("Nuvio")
            .build()
    }
}