package com.nuvio.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
class TrackSelectionScreen(
    carContext: CarContext,
    private val player: ExoPlayer
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        Log.i("NuvioCar", "TrackSelectionScreen: building track list")
        val tracks = player.currentTracks
        val listBuilder = ItemList.Builder()

        listBuilder.addItem(
            Row.Builder()
                .setTitle("🚫 Desativar Legendas")
                .setOnClickListener {
                    Log.i("NuvioCar", "TrackSelectionScreen: disabling text tracks")
                    player.trackSelectionParameters = player.trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .build()
                    screenManager.pop()
                }
                .build()
        )

        tracks.groups.forEach { group ->
            val trackGroup = group.mediaTrackGroup
            val trackType = group.type

            for (i in 0 until trackGroup.length) {
                val format = trackGroup.getFormat(i)
                val isSelected = group.isTrackSelected(i)
                val lang = format.language ?: "auto"
                val label = format.label ?: lang
                
                if (trackType == C.TRACK_TYPE_TEXT) {
                    val title = if (isSelected) "✔ Legenda: $label" else "Legenda: $label"
                    listBuilder.addItem(
                        Row.Builder()
                            .setTitle(title)
                            .addText("Idioma: $lang")
                            .setOnClickListener {
                                Log.i("NuvioCar", "TrackSelectionScreen: selecting subtitle track $i ($lang)")
                                player.trackSelectionParameters = player.trackSelectionParameters
                                    .buildUpon()
                                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                    .setOverrideForType(TrackSelectionOverride(trackGroup, i))
                                    .build()
                                screenManager.pop()
                            }
                            .build()
                    )
                } else if (trackType == C.TRACK_TYPE_AUDIO) {
                    val title = if (isSelected) "✔ Áudio: $label" else "Áudio: $label"
                    listBuilder.addItem(
                        Row.Builder()
                            .setTitle(title)
                            .addText("Idioma: $lang (${format.channelCount} ch)")
                            .setOnClickListener {
                                Log.i("NuvioCar", "TrackSelectionScreen: selecting audio track $i ($lang)")
                                player.trackSelectionParameters = player.trackSelectionParameters
                                    .buildUpon()
                                    .setOverrideForType(TrackSelectionOverride(trackGroup, i))
                                    .build()
                                screenManager.pop()
                            }
                            .build()
                    )
                }
            }
        }

        return ListTemplate.Builder()
            .setTitle("Áudio e Legendas")
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.BACK)
            .build()
    }
}
