package com.elfen.clipkeep.presentation.screen.scroller

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Effects
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.material3.Player
import coil3.request.Disposable
import com.elfen.clipkeep.R

@Composable
fun ScrollerScreen(
    route: ScrollerRoute,
    onBack: () -> Unit
) {
    val viewModel = hiltViewModel<ScrollerViewModel, ScrollerViewModel.Factory>(
        creationCallback = {
            it.create(route.clipId)
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    ScrollerScreen(
        state = state,
        clipId = route.clipId,
        onRotate = viewModel::rotate,
        onBack = onBack
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(UnstableApi::class)
@Composable
private fun ScrollerScreen(
    state: ScrollerUiState,
    clipId: Long? = null,
    onRotate: (id: Long, rotation: Float) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var found by rememberSaveable {
        mutableStateOf(false)
    }
    val pagerState = rememberPagerState(
        pageCount = { state.clips.size }
    )
    var fillScreen by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(state) {
        if (state.clips.fastAny { it.id == clipId } && !found) {
            pagerState.scrollToPage(state.clips.indexOfFirst { it.id == clipId })
            found = true
        }
    }

    Scaffold(
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(onClick = { fillScreen = !fillScreen }) {
                    Icon(
                        painterResource(
                            if (fillScreen)
                                R.drawable.outline_fit_screen_24
                            else
                                R.drawable.outline_fullscreen_24
                        ), null
                    )
                }
            }
        },
        containerColor = Color.Black
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            VerticalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                state = pagerState,
            ) { page ->
                val clip = state.clips[page]
                var rotation by remember { mutableStateOf<Float?>(null) }
                val player = remember(rotation) {
                    ExoPlayer.Builder(context)
                        .build().apply {
                            playWhenReady = true
                            volume = 0f
                        }
                }

                DisposableEffect(rotation) {
                    player.setMediaItem(MediaItem.fromUri(clip.uri))
                    player.prepare()


                    onDispose { player.release() }
                }

                LaunchedEffect(rotation) {
                    Log.d("ScrollerScreen", "clip: $clip")

                    if (rotation != null) {
                        val position = player.currentPosition
                        player.setVideoEffects(
                            listOf(
                                ScaleAndRotateTransformation.Builder()
                                    .setRotationDegrees(rotation!!)
                                    .build()
                            )
                        )
                        player.setMediaItem(player.currentMediaItem!!)
                        player.prepare()
                        player.seekTo(position)
                        player.play()
                    }
                }

                Box {
                    Player(
                        modifier = Modifier
                            .fillMaxSize(),
                        player = player,
                        contentScale = if (fillScreen)
                            ContentScale.Crop
                        else
                            ContentScale.Fit,
                        surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
                        showControls = false,
                        shutter = {}
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                rotation = (rotation ?: 0f).minus(90f)
                            }
                        ) {
                            Icon(painterResource(R.drawable.outline_rotate_right_24), null)
                        }

                        AnimatedVisibility(
                            visible = rotation != null,
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    onRotate(clip.id, rotation!!)
                                }
                            ) {
                                Icon(painterResource(R.drawable.outline_check_24), null)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.baseline_arrow_back_24), null)
                }
            }
        }
    }
}