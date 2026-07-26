package com.elfen.clipkeep.presentation.screen.edit_part

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.material3.Player
import com.elfen.clipkeep.R
import com.elfen.clipkeep.domain.model.Crop
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.presentation.component.PlayerExternalControls
import com.elfen.clipkeep.presentation.state.rememberPlayerState
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import com.elfen.clipkeep.utils.msToText
import java.util.Locale
import kotlin.math.abs
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
        onUpdateCrop = viewModel::updateCrop,
        onConfirm = {
            viewModel.confirm {
                onBack()
            }
        },
        onBack = onBack
    )
}

private enum class Handle {
    TopRight,
    TopLeft,
    BottomRight,
    BottomLeft,
    Middle
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun EditPartScreen(
    state: EditPartUiState = EditPartUiState(),
    onSetRange: (range: ClosedRange<Long>) -> Unit = {},
    onSetPlayerClipping: (shouldClip: Boolean) -> Unit = {},
    onUpdateCrop: (crop: Crop) -> Unit = {},
    onConfirm: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val playerState = rememberPlayerState(state.exoPlayer)
    val density = LocalDensity.current

    LaunchedEffect(state.crop) {
        Log.d(TAG, "Crop Updated: ${state.crop}")
    }

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
                var tempCrop by remember { mutableStateOf<Crop?>(null) }

                var width by remember { mutableFloatStateOf(0f) }
                var height by remember { mutableFloatStateOf(0f) }

                /**
                 * Dimensions of the contained video in pixels
                 */
                val dimensions by remember {
                    derivedStateOf {
                        val containerAspectRatio = if (height == 0f) 0f else width / height
                        val videoAspectRatio =
                            state.edit.width.toFloat() / state.edit.height.toFloat()

                        if (containerAspectRatio > videoAspectRatio) {
                            (height * videoAspectRatio) to height
                        } else {
                            width to (width / videoAspectRatio)
                        }
                    }
                }

                val cropOffset by remember(state.crop) {
                    derivedStateOf {
                        val relativeCropOffset = Offset(
                            (tempCrop ?: state.crop).x / state.edit.width.toFloat(),
                            (tempCrop ?: state.crop).y / state.edit.height.toFloat()
                        )
                        DpOffset(
                            with(density) { (((width - dimensions.first) / 2) + relativeCropOffset.x * dimensions.first).toDp() },
                            with(density) { (((height - dimensions.second) / 2) + relativeCropOffset.y * dimensions.second).toDp() },
                        )
                    }
                }


                val cropSize by remember(state.crop) {
                    derivedStateOf {
                        Log.d(TAG, "EditPartScreen: Current State: ${state.crop}")
                        val relativeSize = Offset(
                            (tempCrop ?: state.crop).width / state.edit.width.toFloat(),
                            (tempCrop ?: state.crop).height / state.edit.height.toFloat()
                        )

                        DpOffset(
                            with(density) { (dimensions.first * relativeSize.x).toDp() },
                            with(density) { (dimensions.second * relativeSize.y).toDp() },
                        )
                    }
                }

                LaunchedEffect(cropSize) {
                    Log.d(TAG, "EditPartScreen: cropSize: $cropSize")
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            width = size.width
                            height = size.height
                        },
                    contentAlignment = Alignment.TopStart
                ) {
                    Player(
                        player = state.exoPlayer,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .align(Alignment.Center),
                        showControls = false,
                        shutter = {},
                        contentScale = ContentScale.Fit,
                        surfaceType = SURFACE_TYPE_TEXTURE_VIEW
                    )

                    var handle by remember { mutableStateOf<Handle?>(null) }

                    Box(
                        modifier = Modifier
                            .size(cropSize.x, cropSize.y)
                            .offset(cropOffset.x, cropOffset.y)
                            .border(2.dp, Color.White)
                            .pointerInput(state.crop) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        Log.d(TAG, "EditPartScreen: offset: $offset")
                                        val handles: List<Pair<Handle, Offset>> = listOf(
                                            Handle.TopLeft to Offset(0f, 0f),
                                            Handle.TopRight to Offset(size.width.toFloat(), 0f),
                                            Handle.BottomLeft to Offset(0f, size.height.toFloat()),
                                            Handle.BottomRight to Offset(
                                                size.width.toFloat(),
                                                size.height.toFloat()
                                            )
                                        )

                                        val selected = handles.firstOrNull { (_, position) ->
                                            abs(position.x - offset.x) < 128 && abs(position.y - offset.y) < 128
                                        } ?: (Handle.Middle to Offset.Zero)

                                        Log.d(TAG, "EditPartScreen: handle: ${selected.first}")
                                        handle = selected.first

                                        tempCrop = state.crop
                                    },
                                    onDrag = { _, dragAmount ->
                                        Log.d(TAG, "Crop: $tempCrop")

                                        var newCrop = when (handle ?: return@detectDragGestures) {
                                            Handle.TopRight -> {
                                                tempCrop!!.copy(
                                                    width = tempCrop!!.width + ((dragAmount.x / dimensions.first) * state.edit.width),
                                                    height = tempCrop!!.height - ((dragAmount.y / dimensions.second) * state.edit.height),
                                                    y = tempCrop!!.y + (dragAmount.y / dimensions.second) * state.edit.height
                                                )
                                            }

                                            Handle.TopLeft -> {
                                                tempCrop!!.copy(
                                                    width = tempCrop!!.width - ((dragAmount.x / dimensions.first) * state.edit.width),
                                                    height = tempCrop!!.height - ((dragAmount.y / dimensions.second) * state.edit.height),
                                                    x = tempCrop!!.x + (dragAmount.x / dimensions.first) * state.edit.width,
                                                    y = tempCrop!!.y + (dragAmount.y / dimensions.second) * state.edit.height,
                                                )
                                            }

                                            Handle.BottomRight -> {
                                                tempCrop!!.copy(
                                                    width = tempCrop!!.width + ((dragAmount.x / dimensions.first) * state.edit.width),
                                                    height = tempCrop!!.height + ((dragAmount.y / dimensions.second) * state.edit.height),
                                                )
                                            }

                                            Handle.BottomLeft -> {
                                                tempCrop!!.copy(
                                                    width = tempCrop!!.width - ((dragAmount.x / dimensions.first) * state.edit.width),
                                                    height = tempCrop!!.height + ((dragAmount.y / dimensions.second) * state.edit.height),
                                                    x = tempCrop!!.x + (dragAmount.x / dimensions.first) * state.edit.width,
                                                )
                                            }

                                            Handle.Middle -> {
                                                tempCrop!!.copy(
                                                    x = tempCrop!!.x + (dragAmount.x / dimensions.first) * state.edit.width,
                                                    y = tempCrop!!.y + (dragAmount.y / dimensions.second) * state.edit.height,
                                                )
                                            }
                                        }

                                        Log.d(TAG, "EditPartScreen: $newCrop")

                                        newCrop = newCrop.copy(
                                            width = newCrop.width.coerceIn(
                                                0f,
                                                state.edit.width.toFloat()
                                            ),
                                            height = newCrop.height.coerceIn(
                                                0f,
                                                state.edit.height.toFloat()
                                            )
                                        )


                                        tempCrop = newCrop.copy(
                                            x = newCrop.x.coerceIn(
                                                0f,
                                                state.edit.width.toFloat() - newCrop.width
                                            ),
                                            y = newCrop.y.coerceIn(
                                                0f,
                                                state.edit.height.toFloat() - newCrop.height
                                            )
                                        )
                                    },
                                    onDragEnd = {
                                        handle = null
                                        onUpdateCrop(tempCrop!!)
                                        tempCrop = null
                                    },
                                    onDragCancel = {
                                        handle = null
                                        tempCrop = null
                                    }
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            HorizontalDivider(color = Color.White)
                            HorizontalDivider(color = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            VerticalDivider(color = Color.White)
                            VerticalDivider(color = Color.White)
                        }
                    }
                }

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
                    var tempStart by remember { mutableStateOf<Long?>(null) }
                    var tempEnd by remember { mutableStateOf<Long?>(null) }
                    var previousPlaybackState by remember { mutableStateOf<Boolean?>(null) }
                    var lastSeek by remember { mutableLongStateOf(0) }

                    RangeSlider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = state.exoPlayer != null,
                        value = (tempStart?.toFloat() ?: state.startMs.toFloat())..(tempEnd
                            ?: state.endMs).toFloat(),
                        onValueChange = {
                            if (initialPosition == null) {
                                initialPosition = state.exoPlayer!!.currentPosition
                                onSetPlayerClipping(false)
                            }


                            val startChanged = it.start.toLong() != state.startMs

                            if (startChanged)
                                tempStart = it.start.toLong()
                            else
                                tempEnd = it.endInclusive.toLong()

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
                                "EditPartScreen: $tempStart, $tempEnd, ${state.startMs}, ${state.endMs}"
                            )
                            onSetRange(
                                (tempStart ?: state.startMs)..(tempEnd ?: state.endMs)
                            )

                            onSetPlayerClipping(true)
                            state.exoPlayer?.seekTo(
                                initialPosition!!.coerceIn(
                                    tempStart ?: state.startMs,
                                    tempEnd ?: state.endMs
                                )
                            )

                            if (previousPlaybackState == true)
                                state.exoPlayer?.play()
                            else if (previousPlaybackState == false) {
                                state.exoPlayer?.pause()
                            }

                            previousPlaybackState = null
                            initialPosition = null
                            tempStart = null
                            tempEnd = null
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