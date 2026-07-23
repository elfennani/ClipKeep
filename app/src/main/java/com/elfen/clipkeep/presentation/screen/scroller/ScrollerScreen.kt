package com.elfen.clipkeep.presentation.screen.scroller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
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
        onBack = onBack
    )
}

@Composable
private fun ScrollerScreen(
    state: ScrollerUiState,
    clipId: Long? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var found by rememberSaveable {
        mutableStateOf(false)
    }
    val pagerState = rememberPagerState(
        pageCount = { state.clips.size }
    )

    LaunchedEffect(state) {
        if (state.clips.fastAny { it.id == clipId } && !found) {
            pagerState.scrollToPage(state.clips.indexOfFirst { it.id == clipId })
            found = true
        }
    }

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
            val player = remember {
                ExoPlayer.Builder(context)
                    .build().apply {
                        playWhenReady = true
                    }
            }

            DisposableEffect(Unit) {
                player.setMediaItem(MediaItem.fromUri(clip.uri))
                player.prepare()

                onDispose { player.release() }
            }

            Player(
                modifier = Modifier.fillMaxSize(),
                player = player,
                contentScale = ContentScale.Fit,
                surfaceType = SURFACE_TYPE_TEXTURE_VIEW
            )
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