package com.nuvio.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.nuvio.app.features.streams.StreamsRepository
import com.nuvio.app.features.streams.StreamItem
import com.nuvio.app.features.debrid.DirectDebridPlaybackResolver
import com.nuvio.app.features.debrid.DirectDebridPlayableResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CarStreamsScreen(
    carContext: CarContext,
    private val type: String,
    private val videoId: String,
    private val parentMetaId: String,
    private val title: String
) : Screen(carContext) {

    init {
        Log.i("NuvioCar", "CarStreamsScreen init: loading streams for type=$type, videoId=$videoId")
        StreamsRepository.load(
            type = type,
            videoId = videoId,
            parentMetaId = parentMetaId,
            manualSelection = false
        )

        lifecycleScope.launch {
            StreamsRepository.uiState.collectLatest { state ->
                Log.i("NuvioCar", "CarStreamsScreen: uiState updated. loading=${state.isAnyLoading}, streams count=${state.allStreams.size}")
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val uiState = StreamsRepository.uiState.value

        if (uiState.isAnyLoading && !uiState.hasAnyStreams) {
            return ListTemplate.Builder()
                .setTitle(title)
                .setLoading(true)
                .setHeaderAction(Action.BACK)
                .build()
        }

        val streams = uiState.allStreams
        if (streams.isEmpty()) {
            return MessageTemplate.Builder("Nenhum stream disponível para reprodução.")
                .setTitle(title)
                .setHeaderAction(Action.BACK)
                .build()
        }

        val listBuilder = ItemList.Builder()
        streams.take(15).forEach { stream ->
            val isTorrent = stream.infoHash != null && stream.url == null
            val label = if (isTorrent) "⚠️ [Torrent P2P] ${stream.streamLabel}" else stream.streamLabel
            
            val rowBuilder = Row.Builder()
                .setTitle(label)
            
            val sub = if (isTorrent) "Requer Debrid configurado" else stream.streamSubtitle
            sub?.takeIf { it.isNotBlank() }?.let {
                rowBuilder.addText(it)
            }

            rowBuilder.setOnClickListener {
                Log.i("NuvioCar", "CarStreamsScreen: stream selected -> ${stream.streamLabel}")
                handleStreamSelection(stream)
            }

            listBuilder.addItem(rowBuilder.build())
        }

        return ListTemplate.Builder()
            .setTitle(title)
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun handleStreamSelection(stream: StreamItem) {
        lifecycleScope.launch {
            try {
                Log.i("NuvioCar", "CarStreamsScreen: handling stream selection -> ${stream.streamLabel}, url=${stream.url}, infoHash=${stream.infoHash}")
                
                var resolvedUrl = stream.playableDirectUrl ?: stream.directPlaybackUrl ?: stream.url
                
                if (resolvedUrl.isNullOrBlank() && DirectDebridPlaybackResolver.shouldResolveToPlayableStream(stream)) {
                    runCatching {
                        val result = DirectDebridPlaybackResolver.resolveToPlayableStream(
                            stream = stream,
                            season = null,
                            episode = null
                        )
                        if (result is DirectDebridPlayableResult.Success) {
                            resolvedUrl = result.stream.playableDirectUrl ?: result.stream.directPlaybackUrl ?: result.stream.url
                        }
                    }.onFailure { err ->
                        Log.e("NuvioCar", "CarStreamsScreen: Debrid resolution failed", err)
                    }
                }

                if (resolvedUrl.isNullOrBlank()) {
                    resolvedUrl = stream.externalOpenUrl
                }

                if (!resolvedUrl.isNullOrBlank()) {
                    Log.i("NuvioCar", "CarStreamsScreen: pushing PlaybackScreen with resolvedUrl=$resolvedUrl")
                    screenManager.push(PlaybackScreen(carContext, resolvedUrl, stream.streamLabel, videoId))
                } else {
                    Log.w("NuvioCar", "CarStreamsScreen: could not resolve playable URL for stream")
                }
            } catch (e: Exception) {
                Log.e("NuvioCar", "CarStreamsScreen: error in handleStreamSelection", e)
            }
        }
    }
}
