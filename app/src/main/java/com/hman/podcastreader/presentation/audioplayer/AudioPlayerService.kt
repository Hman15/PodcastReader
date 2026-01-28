package com.hman.podcastreader.presentation.audioplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.hman.podcastreader.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private val binder = AudioPlayerBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var positionUpdateJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "audio_player_channel"

        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_SKIP_FORWARD = "ACTION_SKIP_FORWARD"
        const val ACTION_SKIP_BACKWARD = "ACTION_SKIP_BACKWARD"
    }

    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializePlayer()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_SKIP_FORWARD -> skipForward()
            ACTION_SKIP_BACKWARD -> skipBackward()
        }
        return START_STICKY
    }

    private fun initializePlayer() {
        exoPlayer =
            ExoPlayer.Builder(this).build().apply {
                addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            updatePlaybackState()
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            updatePlaybackState()
                            if (isPlaying) {
                                startForeground(NOTIFICATION_ID, createNotification())
                                startPositionTracking()
                            } else {
                                stopPositionTracking()
                                // Update notification to show play button
                                val notificationManager =
                                    getSystemService(NotificationManager::class.java)
                                notificationManager.notify(
                                    NOTIFICATION_ID,
                                    createNotification()
                                )
                            }
                        }
                    }
                )
            }
    }

    fun playAudio(audioUrl: String, title: String) {
        exoPlayer?.apply {
            val mediaItem = MediaItem.fromUri(audioUrl)
            setMediaItem(mediaItem)
            prepare()
            play()

            _playbackState.value = _playbackState.value.copy(audioUrl = audioUrl, title = title)
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
    }

    fun skipForward(seconds: Int = 15) {
        exoPlayer?.let { player ->
            val newPosition =
                (player.currentPosition + seconds * 1000).coerceAtMost(player.duration)
            player.seekTo(newPosition)
        }
    }

    fun skipBackward(seconds: Int = 15) {
        exoPlayer?.let { player ->
            val newPosition = (player.currentPosition - seconds * 1000).coerceAtLeast(0)
            player.seekTo(newPosition)
        }
    }

    private fun startPositionTracking() {
        stopPositionTracking()
        positionUpdateJob =
            serviceScope.launch {
                while (isActive && exoPlayer?.isPlaying == true) {
                    updatePlaybackState()
                    delay(100) // Update every 100ms for smooth progress
                }
            }
    }

    private fun stopPositionTracking() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun updatePlaybackState() {
        exoPlayer?.let { player ->
            _playbackState.value =
                _playbackState.value.copy(
                    isPlaying = player.isPlaying,
                    currentPosition = player.currentPosition,
                    duration = player.duration.takeIf { it > 0 } ?: 0
                )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Audio Player",
                    NotificationManager.IMPORTANCE_LOW
                )
                    .apply { description = "Audio playback controls" }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // Intent to open app when notification is clicked
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Action: Skip Backward
        val skipBackwardIntent =
            Intent(this, AudioPlayerService::class.java).apply { action = ACTION_SKIP_BACKWARD }
        val skipBackwardPendingIntent =
            PendingIntent.getService(this, 0, skipBackwardIntent, PendingIntent.FLAG_IMMUTABLE)

        // Action: Play/Pause
        val playPauseIntent =
            Intent(this, AudioPlayerService::class.java).apply { action = ACTION_PLAY_PAUSE }
        val playPausePendingIntent =
            PendingIntent.getService(this, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)

        // Action: Skip Forward
        val skipForwardIntent =
            Intent(this, AudioPlayerService::class.java).apply { action = ACTION_SKIP_FORWARD }
        val skipForwardPendingIntent =
            PendingIntent.getService(this, 2, skipForwardIntent, PendingIntent.FLAG_IMMUTABLE)

        val playPauseIcon =
            if (_playbackState.value.isPlaying) {
                R.drawable.ic_stop
            } else {
                R.drawable.ic_play
            }

        val playPauseText = if (_playbackState.value.isPlaying) "Pause" else "Play"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(_playbackState.value.title)
            .setContentText("Audio Player")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_arrow_back, "Skip -15s", skipBackwardPendingIntent)
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)
            .addAction(R.drawable.ic_arrow_forward, "Skip +15s", skipForwardPendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPositionTracking()
        serviceScope.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val playbackSpeed: Float = 1.0f,
    val audioUrl: String = "",
    val title: String = ""
)
