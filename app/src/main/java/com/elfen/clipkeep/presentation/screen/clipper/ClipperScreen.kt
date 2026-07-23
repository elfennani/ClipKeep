package com.elfen.clipkeep.presentation.screen.clipper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.compose.material3.Player
import androidx.navigation3.runtime.NavKey
import com.elfen.clipkeep.R
import com.elfen.clipkeep.presentation.component.EditPartCard
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import com.elfen.clipkeep.utils.AbsoluteSmoothCornerShape
import kotlinx.coroutines.launch

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
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var titleSheetPartId by remember { mutableStateOf<Long?>(null) }

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
        floatingActionButton = {
            if (state.exoPlayer != null) {
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
        } else if (state.clip != null && state.exoPlayer != null) {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(bottom = 128.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

                items(state.clip.parts) { clipPart ->
                    EditPartCard(
                        modifier = Modifier
                            .fillMaxWidth(),
                        part = clipPart,
                        onClick = {},
                        onToggle = { onUiEvent(ClipperUiEvent.TogglePart(clipPart.id)) },
                        onEditTitle = { titleSheetPartId = clipPart.id }
                    )
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