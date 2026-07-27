package com.elfen.clipkeep.presentation.screen.clipper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.material3.Player
import androidx.navigation3.runtime.NavKey
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder
import coil3.video.videoFrameMillis
import com.elfen.clipkeep.R
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.EditingClipPart
import com.elfen.clipkeep.presentation.component.EditPartCard
import com.elfen.clipkeep.presentation.component.PlayerExternalControls
import com.elfen.clipkeep.presentation.screen.edit_part.EditPartRoute
import com.elfen.clipkeep.presentation.state.PlayerState
import com.elfen.clipkeep.presentation.state.rememberPlayerState
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import com.elfen.clipkeep.utils.AbsoluteSmoothCornerShape
import com.elfen.clipkeep.utils.msToText
import kotlinx.coroutines.launch
import kotlin.time.Clock

@Composable
fun ClipperScreen(
    route: ClipperRoute,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val viewModel = hiltViewModel<ClipperViewModel, ClipperViewModel.Factory>(
        creationCallback = {
            it.create(route.id)
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    ClipperScreen(
        state = state,
        onUiEvent = viewModel::handleUiEvent,
        onClickPart = {
            onNavigate(EditPartRoute(route.id, it.id))
        },
        onNavigateBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClipperScreen(
    state: ClipperUiState = ClipperUiState(),
    onNavigateBack: () -> Unit = {},
    onClickPart: (part: EditingClipPart) -> Unit = {},
    onUiEvent: (ClipperUiEvent) -> Unit = {}
) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var titleSheetPartId by remember { mutableStateOf<Long?>(null) }
    val playerState = rememberPlayerState(state.exoPlayer)
    var temp by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(state.isRendering) {
        if (state.isRendering)
            onNavigateBack()
    }

    if (titleSheetPartId != null) {
        ModalBottomSheet(
            onDismissRequest = { titleSheetPartId = null },
            shape = AbsoluteSmoothCornerShape(
                cornerRadiusTL = 32.dp,
                cornerRadiusTR = 32.dp,
                cornerRadiusBR = 0.dp,
                cornerRadiusBL = 0.dp,
            )
        ) {
            val title =
                rememberTextFieldState(
                    initialText = state.clip?.parts?.first { it.id == titleSheetPartId }?.name ?: ""
                )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = title,
                    label = {
                        Text("Part name")
                    }
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onUiEvent(
                            ClipperUiEvent.UpdatePartName(
                                titleSheetPartId!!,
                                title.text.toString().ifBlank { null }?.trim()
                            )
                        )

                        scope.launch {
                            sheet.hide()
                        }.invokeOnCompletion {
                            titleSheetPartId = null
                        }
                    }
                ) {
                    Text("Confirm")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.baseline_arrow_back_24), null)
                    }
                },
                title = {
                    Text("Clipper")
                },
                actions = {
                    TextButton(
                        onClick = { onUiEvent(ClipperUiEvent.Render) },
                        enabled = !state.isRendering
                    ) {
                        Text("Render")
                    }
                }
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (state.clip != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 128.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stickyHeader {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                        ) {
                            Player(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .aspectRatio(16f / 9),
                                player = state.exoPlayer,
                                showControls = false,
                                shutter = {},
                                surfaceType = SURFACE_TYPE_TEXTURE_VIEW
                            )
                        }
                    }

                    items(state.clip.parts) { clipPart ->
                        EditPartCard(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth(),
                            part = clipPart,
                            onClick = {
                                onClickPart(clipPart)
                            },
                            onToggle = { onUiEvent(ClipperUiEvent.TogglePart(clipPart.id)) },
                            onEditTitle = { titleSheetPartId = clipPart.id }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .clickable {
                                    if (state.exoPlayer != null)
                                        onUiEvent(ClipperUiEvent.AddClip(state.exoPlayer.currentPosition))
                                }
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(painterResource(R.drawable.sharp_bookmark_add_24), null)
                            Text("Add new clip", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(WindowInsets.navigationBars.asPaddingValues())
                ) {
                    HorizontalDivider(
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        PlayerExternalControls(playerState = playerState)

                        Box(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            var lastSeek by remember { mutableLongStateOf(0) }
                            var previousPlaybackState by remember { mutableStateOf<Boolean?>(null) }

                            Slider(
                                value = temp ?: playerState.currentPosition.toFloat(),
                                onValueChange = {
                                    temp = it

                                    if (previousPlaybackState == null)
                                        previousPlaybackState = state.exoPlayer?.isPlaying

                                    state.exoPlayer?.pause()
                                    if (Clock.System.now()
                                            .toEpochMilliseconds() - lastSeek > 200
                                    ) {
                                        state.exoPlayer?.seekTo(it.toLong())
                                        lastSeek = Clock.System.now().toEpochMilliseconds()
                                    }
                                },
                                onValueChangeFinished = {
                                    state.exoPlayer?.seekTo(temp!!.toLong())
                                    if (previousPlaybackState == true)
                                        state.exoPlayer?.play()
                                    else if (previousPlaybackState == false) {
                                        state.exoPlayer?.pause()
                                    }
                                    previousPlaybackState = null
                                    temp = null
                                },
                                valueRange = 0f..(playerState.duration?.toFloat() ?: 0f)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun ClipperScreenPrev() {
    ClipKeepTheme() {
        ClipperScreen(
            state = ClipperUiState(
                isLoading = false,
                exoPlayer = null,
                isRendering = false,
                clip = EditingClip.samples.first()
            )
        )
    }
}