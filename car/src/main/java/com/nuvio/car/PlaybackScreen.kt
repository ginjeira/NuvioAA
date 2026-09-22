package com.nuvio.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import coil.ImageLoader
import coil.request.ImageRequest
import androidx.core.graphics.drawable.toBitmap
import com.nuvio.androidApp.playback.PlayerController
import com.nuvio.androidApp.catalog.models.Series
import com.nuvio.androidApp.catalog.models.Episode

class PlaybackScreen(
    carContext: CarContext,
    private val series: Series,
    private var episode: Episode
) : Screen(carContext) {

    private val player = PlayerController(carContext)
    private val imageLoader = ImageLoader(carContext)
    private var artwork: CarIcon? = null

    init {
        carContext.lifecycleScope.launch {
            startEpisode(episode)
            invalidate()
        }
    }

    private suspend fun startEpisode(ep: Episode) {
        player.play(ep.streamUrl)

        val bitmap = loadArtwork(ep.artworkUrl)
        if (bitmap != null) {
            artwork = CarImageUtils.fromBitmap(bitmap)
        }
    }

    override fun onGetTemplate(): Template {

        val builder = PlaybackTemplate.Builder()

        builder.setTitle("${series.title} — ${episode.title}")

        artwork?.let {
            builder.setArtwork(it)
        }

        // Botão PAUSAR
        builder.setPrimaryAction(
            Action.Builder()
                .setTitle("Pausar")
                .setOnClickListener {
                    player.pause()
                }
                .build()
        )

        // Botão REPRODUZIR
        builder.setSecondaryAction(
            Action.Builder()
                .setTitle("Reproduzir")
                .setOnClickListener {
                    player.resume()
                }
                .build()
        )

        // Botão ANTERIOR
        builder.addAction(
            Action.Builder()
                .setTitle("Anterior")
                .setOnClickListener {
                    val index = series.episodes.indexOf(episode)
                    if (index > 0) {
                        episode = series.episodes[index - 1]
                        carContext.lifecycleScope.launch {
                            startEpisode(episode)
                            invalidate()
                        }
                    }
                }
                .build()
        )

        // Botão SEGUINTE
        builder.addAction(
            Action.Builder()
                .setTitle("Seguinte")
                .setOnClickListener {
                    val index = series.episodes.indexOf(episode)
                    if (index < series.episodes.size - 1) {
                        episode = series.episodes[index + 1]
                        carContext.lifecycleScope.launch {
                            startEpisode(episode)
                            invalidate()
                        }
                    }
                }
                .build()
        )

        return builder.build()
    }

    private suspend fun loadArtwork(url: String) =
        imageLoader.execute(
            ImageRequest.Builder(carContext)
                .data(url)
                .allowHardware(false)
                .build()
        ).drawable?.toBitmap()
}
