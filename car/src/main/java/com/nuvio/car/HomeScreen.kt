package com.nuvio.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*

class HomeScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val itemList = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Catálogo")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(CatalogScreen(carContext))
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Últimos Episódios")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(PlaybackScreen(carContext))
                    }
                    .build()
            )
            .build()

        return PaneTemplate.Builder(itemList)
            .setTitle("Nuvio")
            .build()
    }
}
