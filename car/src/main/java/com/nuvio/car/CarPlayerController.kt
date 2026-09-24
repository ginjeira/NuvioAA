package com.nuvio.car

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession

class CarPlayerController(context: Context) {

    private val player = ExoPlayer.Builder(context).build()
    private val session = MediaSession.Builder(context, player).build()

    fun play(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun release() {
        session.release()
        player.release()
    }
}
