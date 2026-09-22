package com.nuvio.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import coil.ImageLoader
import coil.request.ImageRequest
import androidx.core.graphics.drawable.toBitmap
import com.nuvio.androidApp.catalog.models.Series

class CatalogScreen(carContext: CarContext) : Screen(carContext) {

    private var seriesList: List<Series> = emptyList()
    private val imageLoader = ImageLoader(carContext)
    private val artworkCache = mutableMapOf<String, CarIcon?>()

    init {
        carContext.lifecycleScope.launch {

            // 1) Carregar catálogo real
            seriesList = CarCatalogBridge.getSeries()

            // 2) Carregar artwork dentro do coroutine (OBRIGATÓRIO)
            seriesList.forEach { series ->
                val bitmap = loadArtwork(series.artworkUrl)
                artworkCache[series.id] = bitmap?.let { CarImageUtils.fromBitmap(it) }
            }

            invalidate()
        }
    }

    override fun onGetTemplate(): Template {

        if (seriesList.isEmpty()) {
            return MessageTemplate.Builder("A carregar catálogo…")
                .setTitle("Nuvio")
                .build()
        }

        val list = ItemList.Builder()

        seriesList.forEach { series ->

            val row = Row.Builder()
                .setTitle(series.title)
                .setBrowsable(true)
                .setOnClickListener {
                    screenManager.push(EpisodesScreen(carContext, series))
                }

            // artwork já carregado no init
            artworkCache[series.id]?.let { row.setImage(it) }

            list.addItem(row.build())
        }

        return ListTemplate.Builder()
            .setTitle("Catálogo")
            .setSingleList(list.build())
            .build()
    }

    private suspend fun loadArtwork(url: String) =
        imageLoader.execute(
            ImageRequest.Builder(carContext)
                .data(url)
                .allowHardware(false)
                .build()
        ).drawable?.toBitmap()
}
