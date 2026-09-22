package com.nuvio.car

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import android.content.Context

class CarPlayerController(context: Context) {

    private val player = ExoPlayer.Builder(context).build()
    private val session = MediaSession.Builder(context, player).build()

    fun play(url: String) {
        // TODO: integrar com o backend real do Nuvio
    }
}
