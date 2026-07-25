package com.elfen.clipkeep.presentation.screen.edit_part

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.compose.material3.Player
import com.elfen.clipkeep.R
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.presentation.component.PlayerExternalControls
import com.elfen.clipkeep.presentation.state.rememberPlayerState
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import com.elfen.clipkeep.utils.msToText
import java.util.Locale
import kotlin.time.Clock

private const val TAG = "EditPartScreen"

@Composable
fun EditPartScreen(
    route: EditPartRoute,
    onBack: () -> Unit
) {
    val viewModel = hiltViewModel<EditPartViewModel, EditPartViewModel.Factory>(
        creationCallback = { it.create(route) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    EditPartScreen(
        state = state,
        onSetRange = { range -> viewModel.updateRange(range.start, range.endInclusive) },
        onSetPlayerClipping = viewModel::setPlayerClipping,
        onConfirm = {
            viewModel.confirm {
                onBack()
            }
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun EditPartScreen(
    state: EditPartUiState = EditPartUiState(),
    onSetRange: (range: ClosedRange<Long>) -> Unit = {},
    onSetPlayerClipping: (shouldClip: Boolean) -> Unit = {},
    onConfirm: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val playerState = rememberPlayerState(state.exoPlayer)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.baseline_arrow_back_24), null)
                    }
                },
                title = {
                    Text(text = "Edit Part")
                },
                actions = {
                    IconButton(onClick = { onConfirm() }) {
                        Icon(
                            painterResource(R.drawable.outline_check_24),
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.edit != null && state.part != null) {
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                Player(
                    player = state.exoPlayer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black),
                    showControls = false,
                    shutter = {},
                    contentScale = ContentScale.Fit
                )

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlayerExternalControls(
                        modifier = Modifier,
                        playerState,
                        offsetBy = if (state.isClipped) state.startMs else 0,
                        overrideDuration = state.edit.duration
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "Starts".uppercase(Locale.ROOT),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                state.startMs.msToText(),
                                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "Finishes".uppercase(Locale.ROOT),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.End
                            )
                            Text(
                                state.endMs.msToText(),
                                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                                textAlign = TextAlign.End
                            )
                        }

                    }

                    var initialPosition by remember { mutableStateOf<Long?>(null) }
                    var temp_start by remember { mutableStateOf<Long?>(null) }
                    var temp_end by remember { mutableStateOf<Long?>(null) }
                    var previousPlaybackState by remember { mutableStateOf<Boolean?>(null) }
                    var lastSeek by remember { mutableLongStateOf(0) }

                    RangeSlider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = state.exoPlayer != null,
                        value = (temp_start?.toFloat() ?: state.startMs.toFloat())..(temp_end
                            ?: state.endMs).toFloat(),
                        onValueChange = {
                            if (initialPosition == null) {
                                initialPosition = state.exoPlayer!!.currentPosition
                                onSetPlayerClipping(false)
                            }


                            val startChanged = it.start.toLong() != state.startMs

                            if (startChanged)
                                temp_start = it.start.toLong()
                            else
                                temp_end = it.endInclusive.toLong()

                            if (previousPlaybackState == null)
                                previousPlaybackState = state.exoPlayer?.isPlaying

                            state.exoPlayer?.pause()

                            if (Clock.System.now()
                                    .toEpochMilliseconds() - lastSeek > 200
                            ) {
                                if (startChanged)
                                    state.exoPlayer?.seekTo(it.start.toLong())
                                else
                                    state.exoPlayer?.seekTo(it.endInclusive.toLong())
                                lastSeek = Clock.System.now().toEpochMilliseconds()
                            }
                        },
                        onValueChangeFinished = {
                            Log.d(
                                TAG,
                                "EditPartScreen: $temp_start, $temp_end, ${state.startMs}, ${state.endMs}"
                            )
                            onSetRange(
                                (temp_start ?: state.startMs)..(temp_end ?: state.endMs)
                            )

                            onSetPlayerClipping(true)
                            state.exoPlayer?.seekTo(
                                initialPosition!!.coerceIn(
                                    temp_start ?: state.startMs,
                                    temp_end ?: state.endMs
                                )
                            )

                            if (previousPlaybackState == true)
                                state.exoPlayer?.play()
                            else if (previousPlaybackState == false) {
                                state.exoPlayer?.pause()
                            }

                            previousPlaybackState = null
                            initialPosition = null
                            temp_start = null
                            temp_end = null
                        },
                        valueRange = 0f..state.edit.duration.toFloat(),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditPartScreenPreview() {
    val edit = EditingClip.samples.first()
    val part = EditingClip.samples.first().parts[1]
    ClipKeepTheme() {
        EditPartScreen(
            state = EditPartUiState(
                isLoading = false,
                edit = edit,
                part = part,
                startMs = part.startMs,
                endMs = part.finishMs
            )
        )
    }
}