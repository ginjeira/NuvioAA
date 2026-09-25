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
import com.nuvio.app.features.player.AudioLanguageOption
import com.nuvio.app.features.player.PlayerSettingsRepository
import com.nuvio.app.features.player.SubtitleLanguageOption
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
    private var lastPlaybackPosition: Long = -1L

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

                var player = exoPlayer
                if (player == null) {
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

                    val startPosition = if (lastPlaybackPosition > 0) {
                        lastPlaybackPosition
                    } else {
                        progressEntry?.lastPositionMs?.takeIf { it > 0 } ?: 0L
                    }

                    PlayerSettingsRepository.ensureLoaded()
                    val settings = PlayerSettingsRepository.uiState.value

                    val subLangs = mutableListOf<String>()
                    if (settings.preferredSubtitleLanguage.isNotBlank() && settings.preferredSubtitleLanguage != SubtitleLanguageOption.NONE) {
                        subLangs.add(settings.preferredSubtitleLanguage)
                    }
                    settings.secondaryPreferredSubtitleLanguage?.takeIf { it.isNotBlank() }?.let {
                        subLangs.add(it)
                    }

                    val audioLangs = mutableListOf<String>()
                    if (settings.preferredAudioLanguage.isNotBlank() && settings.preferredAudioLanguage != AudioLanguageOption.DEVICE) {
                        audioLangs.add(settings.preferredAudioLanguage)
                    }
                    settings.secondaryPreferredAudioLanguage?.takeIf { it.isNotBlank() }?.let {
                        audioLangs.add(it)
                    }

                    player = ExoPlayer.Builder(carContext)
                        .setSeekBackIncrementMs(10000L)
                        .setSeekForwardIncrementMs(10000L)
                        .build().apply {
                            setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
                            setHandleAudioBecomingNoisy(true)

                            val trackParamsBuilder = trackSelectionParameters.buildUpon()
                            if (subLangs.isNotEmpty()) {
                                trackParamsBuilder.setPreferredTextLanguages(*subLangs.toTypedArray())
                            }
                            if (audioLangs.isNotEmpty()) {
                                trackParamsBuilder.setPreferredAudioLanguages(*audioLangs.toTypedArray())
                            }
                            trackSelectionParameters = trackParamsBuilder.build()

                            addListener(object : Player.Listener {
                                override fun onPlayerError(error: PlaybackException) {
                                    Log.e("NuvioCar", "PlaybackScreen ExoPlayer error: ${error.message}", error)
                                }
                                override fun onIsPlayingChanged(isPlaying: Boolean) {
                                    Log.i("NuvioCar", "PlaybackScreen ExoPlayer isPlaying=$isPlaying")
                                    invalidate()
                                }
                            })

                            setMediaItem(mediaItem, startPosition)
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
                }

                playerView.player = player
                rootLayout.addView(playerView)

                presentation?.setContentView(rootLayout)
                presentation?.show()
                Log.i("NuvioCar", "PlaybackScreen: Presentation shown with persistent ExoPlayer!")
            } catch (e: Exception) {
                Log.e("NuvioCar", "PlaybackScreen: Error setting up car video surface", e)
            }
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
            try {
                Log.i("NuvioCar", "PlaybackScreen: onSurfaceDestroyed called")
                exoPlayer?.let { player ->
                    lastPlaybackPosition = player.currentPosition
                }
                
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
        val trackAction = Action.Builder()
            .setTitle("💬 Legendas/Áudio")
            .setOnClickListener {
                exoPlayer?.let { player ->
                    screenManager.push(TrackSelectionScreen(carContext, player))
                }
            }
            .build()

        val actionStrip = ActionStrip.Builder()
            .addAction(trackAction)
            .addAction(Action.BACK)
            .build()

        return NavigationTemplate.Builder()
            .setActionStrip(actionStrip)
            .build()
    }
}
