package com.nuvio.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
class TrackSelectionScreen(
    carContext: CarContext,
    private val player: ExoPlayer
) : Screen(carContext) {

    private data class TrackInfo(
        val group: Tracks.Group,
        val groupIndex: Int,
        val trackIndex: Int,
        val trackType: Int,
        val title: String,
        val isSelected: Boolean
    )

    override fun onGetTemplate(): Template {
        Log.i("NuvioCar", "TrackSelectionScreen: building track list")
        val tracks = player.currentTracks
        val listBuilder = ItemList.Builder()

        val audioTracks = mutableListOf<TrackInfo>()
        val subtitleTracks = mutableListOf<TrackInfo>()

        tracks.groups.forEachIndexed { groupIndex, group ->
            val trackGroup = group.mediaTrackGroup
            val trackType = group.type

            for (i in 0 until trackGroup.length) {
                val format = trackGroup.getFormat(i)
                val isSelected = group.isTrackSelected(i)
                val lang = format.language ?: "auto"
                val label = format.label ?: lang

                val info = TrackInfo(
                    group = group,
                    groupIndex = groupIndex,
                    trackIndex = i,
                    trackType = trackType,
                    title = label,
                    isSelected = isSelected
                )

                if (trackType == C.TRACK_TYPE_AUDIO) {
                    audioTracks.add(info)
                } else if (trackType == C.TRACK_TYPE_TEXT) {
                    subtitleTracks.add(info)
                }
            }
        }

        // Sort selected tracks to the top
        audioTracks.sortByDescending { it.isSelected }
        subtitleTracks.sortByDescending { it.isSelected }

        if (audioTracks.isNotEmpty()) {
            audioTracks.forEach { track ->
                val title = if (track.isSelected) "✔ Áudio: ${track.title}" else "Áudio: ${track.title}"
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(title)
                        .setOnClickListener {
                            Log.i("NuvioCar", "TrackSelectionScreen: switching audio track to ${track.title}")
                            player.trackSelectionParameters = player.trackSelectionParameters
                                .buildUpon()
                                .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                                .addOverride(TrackSelectionOverride(track.group.mediaTrackGroup, track.trackIndex))
                                .build()
                            screenManager.pop()
                        }
                        .build()
                )
            }
        }

        if (subtitleTracks.isNotEmpty()) {
            subtitleTracks.forEach { track ->
                val title = if (track.isSelected) "✔ Legenda: ${track.title}" else "Legenda: ${track.title}"
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(title)
                        .setOnClickListener {
                            Log.i("NuvioCar", "TrackSelectionScreen: switching subtitle track to ${track.title}")
                            player.trackSelectionParameters = player.trackSelectionParameters
                                .buildUpon()
                                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                .addOverride(TrackSelectionOverride(track.group.mediaTrackGroup, track.trackIndex))
                                .build()
                            screenManager.pop()
                        }
                        .build()
                )
            }
        }

        listBuilder.addItem(
            Row.Builder()
                .setTitle("🚫 Desativar Legendas")
                .setOnClickListener {
                    Log.i("NuvioCar", "TrackSelectionScreen: disabling subtitles")
                    player.trackSelectionParameters = player.trackSelectionParameters
                        .buildUpon()
                        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .build()
                    screenManager.pop()
                }
                .build()
        )

        return ListTemplate.Builder()
            .setTitle("Áudio e Legendas")
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.BACK)
            .build()
    }
}
