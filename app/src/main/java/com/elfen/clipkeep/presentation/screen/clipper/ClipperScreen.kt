package com.elfen.clipkeep.presentation.screen.clipper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.compose.material3.Player
import androidx.navigation3.runtime.NavKey
import com.elfen.clipkeep.R
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import com.elfen.clipkeep.utils.msToText

@Composable
fun ClipperScreen(
    route: ClipperRoute,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val viewModel = hiltViewModel<ClipperViewModel, ClipperViewModel.Factory>(
        creationCallback = {
            it.create(route.uri.toUri())
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    ClipperScreen(
        state = state,
        onUiEvent = viewModel::handleUiEvent,
        onNavigateBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClipperScreen(
    state: ClipperUiState = ClipperUiState(),
    onNavigateBack: () -> Unit = {},
    onUiEvent: (ClipperUiEvent) -> Unit = {}
) {
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
        floatingActionButton = {
            if (state.uri != null && state.exoPlayer != null) {
                FloatingActionButton(onClick = {
                    onUiEvent(ClipperUiEvent.AddClip(state.exoPlayer.currentPosition))
                }) {
                    Icon(painterResource(R.drawable.sharp_bookmark_add_24), null)
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                CircularProgressIndicator()
            }
        } else if (state.uri != null && state.exoPlayer != null) {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(bottom = 128.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                stickyHeader {
                    Player(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9)
                            .background(Color.Black),
                        player = state.exoPlayer
                    )
                }

                items(state.clips) { clipPart ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Clip #${clipPart.id + 1}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                TextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = clipPart.startMs.msToText(),
                                    onValueChange = {},
                                    label = {
                                        Text("Start Seconds")
                                    },
                                    enabled = false,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )

                                TextButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        onUiEvent(
                                            ClipperUiEvent.SetClipStartTime(
                                                clipPart.id,
                                                state.exoPlayer.currentPosition
                                            )
                                        )
                                    }
                                ) {
                                    Text("Stamp")
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                TextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = clipPart.finishMs.msToText(),
                                    onValueChange = {},
                                    label = {
                                        Text("End Seconds")
                                    },
                                    enabled = false,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )

                                TextButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        onUiEvent(
                                            ClipperUiEvent.SetClipEndTime(
                                                clipPart.id,
                                                state.exoPlayer.currentPosition
                                            )
                                        )
                                    }
                                ) {
                                    Text("Stamp")
                                }
                            }
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
        ClipperScreen()
    }
}