package com.hman.podcastreader.presentation.audioplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hman.podcastreader.R

@Composable
fun AudioPlayerView(
    audioUrl: String,
    title: String,
    onCollapse: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AudioPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()

    // Bind to service
    DisposableEffect(audioUrl) {
        val intent = Intent(context, AudioPlayerService::class.java)
        context.startService(intent)

        val connection =
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as AudioPlayerService.AudioPlayerBinder
                    viewModel.bindService(binder.getService())
                    viewModel.playAudio(audioUrl, title)
                }

                override fun onServiceDisconnected(name: ComponentName?) {}
            }

        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose { context.unbindService(connection) }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with collapse and close buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Collapse"
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = playbackState.title.ifEmpty { title },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Seek bar
            AudioSeekBar(
                currentPosition = playbackState.currentPosition,
                duration = playbackState.duration,
                onSeek = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip backward
                IconButton(onClick = { viewModel.skipBackward() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Skip backward 15s",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play/Pause
                FilledIconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        painter =
                            painterResource(
                                if (playbackState.isPlaying) R.drawable.ic_stop
                                else R.drawable.ic_play
                            ),
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Skip forward
                IconButton(onClick = { viewModel.skipForward() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_forward),
                        contentDescription = "Skip forward 15s",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback speed control
            PlaybackSpeedControl(
                currentSpeed = playbackState.playbackSpeed,
                onSpeedChange = { viewModel.setPlaybackSpeed(it) }
            )
        }
    }
}

@Composable
fun AudioSeekBar(
    currentPosition: Long, duration: Long, onSeek: (Long) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }

    // Update slider when not seeking
    LaunchedEffect(currentPosition) {
        if (!isUserSeeking && duration > 0) {
            sliderPosition = (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
        }
    }

    Column {
        Slider(value = sliderPosition, onValueChange = {
            isUserSeeking = true
            sliderPosition = it
        }, onValueChangeFinished = {
            isUserSeeking = false
            val newPosition = (sliderPosition * duration).toLong()
            onSeek(newPosition)
        })

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition), style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatTime(duration), style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PlaybackSpeedControl(
    currentSpeed: Float, onSpeedChange: (Float) -> Unit
) {
    val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        speeds.forEach { speed ->
            FilterChip(
                selected = currentSpeed == speed,
                onClick = { onSpeedChange(speed) },
                label = { Text("${speed}x") })
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
