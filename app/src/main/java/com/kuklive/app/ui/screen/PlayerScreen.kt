package com.kuklive.app.ui.screen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.CircularProgressIndicator
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.kuklive.app.ui.MainViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val channels = viewModel.playingChannels

    if (channels.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Nothing to play", color = Color.White)
        }
        return
    }

    var index by remember { mutableIntStateOf(viewModel.playingIndex.coerceIn(0, channels.lastIndex)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasStarted by remember { mutableStateOf(false) }
    // Auto-skip dead streams so the user lands on a working channel hands-free.
    var consecutiveFails by remember { mutableIntStateOf(0) }
    val maxAutoSkip = 15
    val canAutoSkip = errorMessage != null && index < channels.lastIndex && consecutiveFails <= maxAutoSkip

    val exoPlayer = remember {
        // Most IPTV streams redirect between http/https and require a
        // browser-like User-Agent — ExoPlayer blocks both by default.
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Kuklive/1.0")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30_000)
            .setReadTimeoutMs(30_000)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build().apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        isBuffering = state == Player.STATE_BUFFERING
                        if (state == Player.STATE_READY) {
                            hasStarted = true
                            consecutiveFails = 0
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        isBuffering = false
                        consecutiveFails++
                        errorMessage = "Stream unavailable (${error.errorCodeName})"
                    }
                })
            }
    }

    // (Re)load whenever the selected channel changes.
    LaunchedEffect(index) {
        errorMessage = null
        isBuffering = true
        hasStarted = false
        val channel = channels[index]
        viewModel.updatePlayingIndex(index)
        val mediaItem = MediaItem.Builder()
            .setUri(channel.url)
            .apply {
                if (channel.url.contains(".m3u8", ignoreCase = true)) {
                    setMimeType(MimeTypes.APPLICATION_M3U8)
                }
            }
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        // If nothing plays within 15s, treat it as unavailable instead of spinning forever.
        delay(15_000)
        if (!hasStarted && errorMessage == null) {
            isBuffering = false
            consecutiveFails++
            errorMessage = "Stream not responding — it may be offline or geo-blocked."
        }
    }

    // Auto-advance to the next channel a few seconds after a failure, until one
    // plays or too many in a row fail (then we stop and let the user decide).
    LaunchedEffect(errorMessage, index) {
        if (canAutoSkip) {
            delay(4_000)
            if (errorMessage != null) index++
        }
    }

    // Keep the screen awake the whole time a channel is open, then release
    // the player and clear the flag when leaving the player screen.
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            exoPlayer.release()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (isBuffering && errorMessage == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        if (errorMessage != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp),
                ) {
                    Text(errorMessage!!, color = Color.White, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (canAutoSkip) {
                            "Trying next channel…"
                        } else {
                            "Many channels here are offline. Try another category, or add your own playlist in Settings."
                        },
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Top bar: back + channel name.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x99000000))
                .padding(8.dp),
        ) {
            IconButton(onClick = {
                exoPlayer.stop()
                onBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = channels[index].name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        // Bottom zapping controls.
        if (channels.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                IconButton(
                    onClick = { if (index > 0) index-- },
                    enabled = index > 0,
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous channel",
                        tint = if (index > 0) Color.White else Color.Gray,
                    )
                }
                Spacer(Modifier.width(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${index + 1} / ${channels.size}", color = Color.White)
                }
                Spacer(Modifier.width(32.dp))
                IconButton(
                    onClick = { if (index < channels.lastIndex) index++ },
                    enabled = index < channels.lastIndex,
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next channel",
                        tint = if (index < channels.lastIndex) Color.White else Color.Gray,
                    )
                }
            }
        }
    }
}

/** Walks the ContextWrapper chain to find the hosting Activity. */
private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
