package com.nuvio.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class CatalogScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {

        val list = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Nuvio Auto")
                    .build()
            )
            .build()

        return ListTemplate.Builder()
            .setTitle("Catálogo")
            .setSingleList(list)
            .build()
    }
}