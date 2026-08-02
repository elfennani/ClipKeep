package com.elfen.clipkeep.presentation.screen.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.elfen.clipkeep.R
import com.elfen.clipkeep.domain.model.Clip
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.presentation.component.ClipCard
import com.elfen.clipkeep.presentation.screen.clip.ClipRoute
import com.elfen.clipkeep.presentation.screen.clipper.ClipperRoute
import com.elfen.clipkeep.presentation.screen.scroller.ScrollerRoute
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import com.elfen.clipkeep.utils.AbsoluteSmoothCornerShape
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun HomeScreen(
    onNavigate: (NavKey) -> Unit
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle();
    HomeScreen(
        state = state,
        onNavigateToClipper = {
            onNavigate(ClipperRoute(it))
        },
        onClickClip = {
            onNavigate(ScrollerRoute(it.id))
        },
        onCreateEdit = viewModel::createEdit,
        onDeleteClip = viewModel::deleteClip,
        onRandomizeClips = viewModel::randomizeClips,
        onToggleRandomization = viewModel::toggleRandomization
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: HomeUiState = HomeUiState(listState = rememberLazyStaggeredGridState()),
    onNavigateToClipper: (id: Long) -> Unit = {},
    onClickClip: (clip: Clip) -> Unit = {},
    onCreateEdit: (uri: Uri, onCreated: (EditingClip) -> Unit) -> Unit = { _, _ -> },
    onRandomizeClips: () -> Unit = {},
    onToggleRandomization: () -> Unit = {},
    onDeleteClip: (id: Long) -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(true)
    val scope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                context.contentResolver.takePersistableUriPermission(uri, takeFlags)

                onCreateEdit(uri) {
                    onNavigateToClipper(it.id)
                }
            }
        }
    var selectedClipId by remember { mutableStateOf<Long?>(null) }

    if (selectedClipId != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                selectedClipId = null
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDeleteClip(selectedClipId!!)

                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            selectedClipId = null
                        }
                    },
                ) {
                    Text("Delete Clip")
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnimatedVisibility(visible = state.settings.isRandomized) {
                        SmallFloatingActionButton(onClick = { onRandomizeClips() }) {
                            Icon(
                                painterResource(R.drawable.outline_screen_rotation_alt_24),
                                null,
                            )
                        }
                    }
                    SmallFloatingActionButton(onClick = { onToggleRandomization() }) {
                        Icon(
                            painterResource(R.drawable.outline_sort_by_alpha_24),
                            null,
                            modifier = Modifier.alpha(if (state.settings.isRandomized) 0.5f else 1f)
                        )
                    }
                }
                ExtendedFloatingActionButton(onClick = {
                    launcher.launch(arrayOf("video/*"))
                }) {
                    Icon(painterResource(R.drawable.sharp_video_camera_back_add_24), null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import")
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (state.clips.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nothing to see here!")
            }
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier,
                state = state.listState,
                columns = StaggeredGridCells.Fixed(3),
                verticalItemSpacing = 4.dp,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(0.dp) + innerPadding,
            ) {
                item(span = StaggeredGridItemSpan.FullLine) { }
                items(
                    items = state.clips,
                    key = { clip -> clip.id },
                    span = { clip ->
                        val isRotated = abs(clip.rotation % 180f) != 0f
                        val aspectRatio =
                            if (isRotated) clip.height.toFloat() / clip.width else clip.width.toFloat() / clip.height

                        if ((aspectRatio) >= 1f) {
                            StaggeredGridItemSpan.FullLine
                        } else {
                            StaggeredGridItemSpan.SingleLane
                        }
                    },
                    contentType = { clip ->
                        val isRotated = abs(clip.rotation % 180f) != 0f
                        val aspectRatio =
                            if (isRotated) clip.height.toFloat() / clip.width else clip.width.toFloat() / clip.height

                        if ((aspectRatio) >= 1f) {
                            "FullLine"
                        } else {
                            "SingleLane"
                        }
                    }
                ) { clip ->
                    ClipCard(
                        modifier = Modifier
                            .combinedClickable(
                                onLongClick = { selectedClipId = clip.id },
                                onClick = { onClickClip(clip) }
                            ),
                        clip = clip
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPrev() {
    ClipKeepTheme {
        HomeScreen()
    }
}

@Preview
@Composable
private fun HomeScreenClipsPreview() {
    ClipKeepTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                clips = Clip.samples,
                listState = rememberLazyStaggeredGridState()
            )
        )
    }
}

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    ClipKeepTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                clips = emptyList(),
                listState = rememberLazyStaggeredGridState()
            )
        )
    }
}