package com.elfen.clipkeep.presentation.state

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

private const val TAG = "PlayerState"

class PlayerState(private val player: Player?, private val coroutineScope: CoroutineScope) {
    var isPlaying by mutableStateOf(false)
        private set
    var currentPosition by mutableLongStateOf(0)
        private set
    var duration by mutableStateOf<Long?>(null)
        private set

    var job: Job? = null

    init {
        onStart()
    }

    private var listener: Player.Listener? = null

    private fun update() {
        if (player != null) {
            isPlaying = player.isPlaying
            duration = player.duration.let {
                if (it == C.TIME_UNSET)
                    null
                else
                    it
            }
            currentPosition = player.currentPosition
        }
    }

    private fun onStart() {
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                update()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                update()
            }
        }.also { listener = it })
        update()

        if (player != null)
            job = coroutineScope.launch {
                while (isActive) {
                    Log.d(TAG, "onStart: Updating player state")
                    update()

                    delay(1_000)
                }
            }
    }

    fun togglePlayback() {
        if (player != null) {
            if (player.isPlaying)
                player.pause()
            else
                player.play()
        }
    }

    fun seekBy(byMs: Long) {
        player?.seekTo(player.currentPosition + byMs)
    }

    fun onDestroy() {
        listener?.let { player?.removeListener(it) }
        job?.cancel()
        job = null
    }
}

@Composable
fun rememberPlayerState(
    player: Player?
): PlayerState {
    val coroutineScope = rememberCoroutineScope()
    val state = remember(player) { PlayerState(player, coroutineScope) }

    DisposableEffect(player) {
        onDispose {
            state.onDestroy()
        }
    }

    return state
}