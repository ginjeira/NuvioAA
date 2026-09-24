package com.nuvio.car

import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.util.Log
import android.widget.FrameLayout
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerView
import com.nuvio.app.features.watchprogress.WatchProgressRepository

class PlaybackScreen(
    carContext: CarContext,
    private val videoUrl: String,
    private val contentTitle: String,
    private val videoId: String = ""
) : Screen(carContext) {

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: Presentation? = null

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
            try {
                Log.i("NuvioCar", "PlaybackScreen: onSurfaceAvailable w=${surfaceContainer.width}, h=${surfaceContainer.height}")

                val displayManager = carContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                
                virtualDisplay = displayManager.createVirtualDisplay(
                    "NuvioCarVideoDisplay",
                    surfaceContainer.width,
                    surfaceContainer.height,
                    surfaceContainer.dpi,
                    surfaceContainer.surface,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
                )

                val display = virtualDisplay?.display ?: return
                presentation = Presentation(carContext, display)

                val rootLayout = FrameLayout(carContext)
                val playerView = PlayerView(carContext).apply {
                    useController = false
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(videoUrl)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(contentTitle)
                            .setArtist("Nuvio Auto")
                            .setIsPlayable(true)
                            .build()
                    )
                    .build()

                WatchProgressRepository.ensureLoaded()
                val progressEntry = if (videoId.isNotBlank()) {
                    WatchProgressRepository.uiState.value.byVideoId[videoId]
                        ?: WatchProgressRepository.progressForVideo(videoId)
                } else null
                val resumePosition = progressEntry?.lastPositionMs?.takeIf { it > 0 } ?: 0L
                if (resumePosition > 0) {
                    Log.i("NuvioCar", "PlaybackScreen: Resuming playback from saved position $resumePosition ms")
                }

                val player = ExoPlayer.Builder(carContext)
                    .setSeekBackIncrementMs(10000L)
                    .setSeekForwardIncrementMs(10000L)
                    .build().apply {
                        setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
                        setHandleAudioBecomingNoisy(true)

                        trackSelectionParameters = trackSelectionParameters
                            .buildUpon()
                            .setPreferredTextLanguage("pt")
                            .build()

                        addListener(object : Player.Listener {
                            override fun onPlayerError(error: PlaybackException) {
                                Log.e("NuvioCar", "PlaybackScreen ExoPlayer error: ${error.message}", error)
                            }
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                Log.i("NuvioCar", "PlaybackScreen ExoPlayer isPlaying=$isPlaying")
                                invalidate()
                            }
                        })

                        setMediaItem(mediaItem, resumePosition)
                        prepare()
                        play()
                    }
                exoPlayer = player

                val forwardingPlayer = object : ForwardingPlayer(player) {
                    override fun getAvailableCommands(): Player.Commands {
                        return super.getAvailableCommands()
                            .buildUpon()
                            .add(COMMAND_SEEK_TO_NEXT)
                            .add(COMMAND_SEEK_TO_PREVIOUS)
                            .add(COMMAND_SEEK_FORWARD)
                            .add(COMMAND_SEEK_BACK)
                            .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                            .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                            .build()
                    }

                    override fun isCommandAvailable(command: Int): Boolean {
                        return when (command) {
                            COMMAND_SEEK_TO_NEXT,
                            COMMAND_SEEK_TO_PREVIOUS,
                            COMMAND_SEEK_FORWARD,
                            COMMAND_SEEK_BACK,
                            COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                            COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> true
                            else -> super.isCommandAvailable(command)
                        }
                    }

                    override fun seekToNext() {
                        player.seekTo((player.currentPosition + 10000L).coerceAtMost(player.duration))
                    }

                    override fun seekToNextMediaItem() {
                        seekToNext()
                    }

                    override fun seekToPrevious() {
                        player.seekTo((player.currentPosition - 10000L).coerceAtLeast(0L))
                    }

                    override fun seekToPreviousMediaItem() {
                        seekToPrevious()
                    }
                }

                val sessionCallback = object : MediaSession.Callback {
                    @Suppress("UnstableApiUsage")
                    override fun onConnect(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo
                    ): MediaSession.ConnectionResult {
                        Log.i("NuvioCar", "MediaSession: controller connected -> ${controller.packageName}")
                        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                            .setAvailableSessionCommands(MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS)
                            .setAvailablePlayerCommands(MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS)
                            .build()
                    }
                }

                mediaSession = MediaSession.Builder(carContext, forwardingPlayer)
                    .setCallback(sessionCallback)
                    .build()

                playerView.player = player
                rootLayout.addView(playerView)

                presentation?.setContentView(rootLayout)
                presentation?.show()
                Log.i("NuvioCar", "PlaybackScreen: Presentation shown with ForwardingPlayer and full media bar controls!")
            } catch (e: Exception) {
                Log.e("NuvioCar", "PlaybackScreen: Error setting up car video surface", e)
            }
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
            try {
                Log.i("NuvioCar", "PlaybackScreen: onSurfaceDestroyed called")
                mediaSession?.release()
                mediaSession = null

                exoPlayer?.stop()
                exoPlayer?.release()
                exoPlayer = null
                
                presentation?.dismiss()
                presentation = null
                
                virtualDisplay?.release()
                virtualDisplay = null
            } catch (e: Exception) {
                Log.e("NuvioCar", "PlaybackScreen: Error tearing down car video surface", e)
            }
        }
    }

    init {
        Log.i("NuvioCar", "PlaybackScreen init for $contentTitle")
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(surfaceCallback)
    }

    override fun onGetTemplate(): Template {
        Log.i("NuvioCar", "PlaybackScreen: onGetTemplate called")
        val actionStrip = ActionStrip.Builder()
            .addAction(Action.BACK)
            .build()

        return NavigationTemplate.Builder()
            .setActionStrip(actionStrip)
            .build()
    }
}
