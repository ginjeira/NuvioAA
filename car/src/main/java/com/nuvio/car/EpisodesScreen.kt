package com.nuvio.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import com.nuvio.androidApp.catalog.models.Series
import com.nuvio.androidApp.catalog.models.Episode

class EpisodesScreen(
    carContext: CarContext,
    private val series: Series
) : Screen(carContext) {

    private val imageLoader = ImageLoader(carContext)
    private var episodes: List<Episode> = emptyList()

    init {
        carContext.lifecycleScope.launch {
            episodes = series.episodes
            invalidate()
        }
    }

    override fun onGetTemplate(): Template {

        if (episodes.isEmpty()) {
            return MessageTemplate.Builder("A carregar episódios…")
                .setTitle(series.title)
                .build()
        }

        val list = ItemList.Builder()

        episodes.forEach { episode ->

            val artworkBitmap = loadArtwork(episode.artworkUrl)

            val row = Row.Builder()
                .setTitle(episode.title)
                .setBrowsable(true)
                .setOnClickListener {
                    screenManager.push(
                        PlaybackScreen(carContext, series, episode)
                    )
                }

            if (artworkBitmap != null) {
                row.setImage(CarImageUtils.fromBitmap(artworkBitmap))
            }

            list.addItem(row.build())
        }

        return ListTemplate.Builder()
            .setTitle("Episódios de ${series.title}")
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
