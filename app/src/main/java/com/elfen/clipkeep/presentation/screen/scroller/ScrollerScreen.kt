@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.elfen.clipkeep.presentation.screen.scroller

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material.icons.sharp.Start
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.material3.Player
import com.elfen.clipkeep.R
import com.elfen.clipkeep.domain.model.Clip
import com.elfen.clipkeep.domain.model.VideoScalingMode
import com.elfen.clipkeep.domain.model.next
import com.elfen.clipkeep.presentation.component.PlayerExternalControls
import com.elfen.clipkeep.presentation.state.rememberPlayerState
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import kotlin.math.abs
import kotlin.time.Clock

private const val TAG = "ScrollerScreen"

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
        onToggleFullscreen = viewModel::toggleFullscreen,
        onSetClipStartMoment = viewModel::setClipStartMoment,
        onBack = onBack
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(UnstableApi::class)
@Composable
private fun ScrollerScreen(
    state: ScrollerUiState,
    isPreview: Boolean = false,
    clipId: Long? = null,
    onRotate: (id: Long, rotation: Float) -> Unit = { _, _ -> },
    onToggleFullscreen: () -> Unit = {},
    onSetClipStartMoment: (clip: Clip, moment: Long) -> Unit = { _, _ -> },
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var found by rememberSaveable {
        mutableStateOf(false)
    }
    val pagerState = rememberPagerState(
        pageCount = { state.clips.size }
    )
    var hideControls by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state.clips.fastAny { it.id == clipId } && !found) {
            pagerState.scrollToPage(state.clips.indexOfFirst { it.id == clipId })
            found = true
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            VerticalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isPreview) Color.Gray else Color.Black),
                state = pagerState,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState, snapAnimationSpec = spring(
                        stiffness = Spring.StiffnessMedium,
                        visibilityThreshold = Int.VisibilityThreshold.toFloat(),
                    )
                ),
                key = { state.clips.getOrNull(it)?.id ?: it }
            ) { page ->
                val clip = state.clips[page]
                var rotation by remember { mutableStateOf<Float?>(null) }
                var temp by remember { mutableStateOf<Float?>(null) }
                val player = remember(rotation) {
                    if (isPreview) return@remember null

                    ExoPlayer.Builder(context)
                        .build().apply {
                            playWhenReady = true
                            repeatMode = ExoPlayer.REPEAT_MODE_ALL
                            volume = 0f
                        }
                }
                val playerState = rememberPlayerState(player)

                DisposableEffect(rotation) {
                    if (player == null) return@DisposableEffect onDispose { }
                    player.setMediaItem(MediaItem.fromUri(clip.uri))
                    player.prepare()
                    player.seekTo(clip.startMoment)

                    onDispose { player.release() }
                }

                LaunchedEffect(pagerState.currentPage == page) {
                    if (pagerState.currentPage == page)
                        player?.play()
                    else
                        player?.pause()
                }

                LaunchedEffect(rotation) {
                    Log.d("ScrollerScreen", "clip: $clip")

                    if (player == null) return@LaunchedEffect

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

                val clipIsRotated =
                    (abs(clip.rotation % 360)) == 90f || abs(clip.rotation % 360) == 270f
                val clipAspectRatio =
                    if (clipIsRotated) clip.height.toFloat() / clip.width else clip.width.toFloat() / clip.height

                Log.d(TAG, "ScrollerScreen: ${clip.rotation % 360}")
                Log.d(TAG, "ScrollerScreen: $clipIsRotated")
                Log.d(TAG, "ScrollerScreen: $clipAspectRatio")


                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    Player(
                        modifier = Modifier
                            .then(
                                if (state.settings.scalingMode == VideoScalingMode.SCALE_TO_9_16)
                                    Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .aspectRatio(9f / 16)
                                        .clip(RectangleShape)
                                else
                                    Modifier
                                        .fillMaxSize()
                            )
                            .clickable(indication = null, interactionSource = interactionSource) {
                                hideControls = !hideControls
                            },
                        player = player,
                        contentScale = if (state.settings.scalingMode != VideoScalingMode.SCALE_TO_FIT && clipAspectRatio < 4f / 3)
                            ContentScale.Crop
                        else
                            ContentScale.Fit,
                        surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
                        showControls = false,
                        shutter = {}
                    )

                    var expanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .padding(WindowInsets.statusBars.asPaddingValues()),
                    ) {
                        IconButton(
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(0.25f),
                                contentColor = Color.White
                            ),
                            onClick = { expanded = !expanded }
                        ) {
                            Icon(Icons.Sharp.MoreVert, null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("This moment as start") },
                                onClick = {
                                    onSetClipStartMoment(
                                        clip,
                                        player?.currentPosition ?: 0
                                    )
                                },
                                leadingIcon = { Icon(Icons.Sharp.Start, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = { },
                                leadingIcon = { Icon(Icons.Sharp.Delete, null) },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error,
                                    leadingIconColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !hideControls,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f),
                                        )
                                    )
                                )
                                .padding(16.dp)
                                .padding(WindowInsets.navigationBars.asPaddingValues()),
                        ) {
                            CompositionLocalProvider(LocalContentColor provides Color.White) {
                                PlayerExternalControls(playerState = playerState)

                                var lastSeek by remember { mutableLongStateOf(0) }
                                var previousPlaybackState by remember {
                                    mutableStateOf<Boolean?>(
                                        null
                                    )
                                }
                                val interactionSource = remember { MutableInteractionSource() }

                                Slider(
                                    value = if (isPreview) 33f else (temp
                                        ?: playerState.currentPosition.toFloat()),
                                    onValueChange = {
                                        temp = it

                                        if (previousPlaybackState == null)
                                            previousPlaybackState = player?.isPlaying

                                        player?.pause()
                                        if (Clock.System.now()
                                                .toEpochMilliseconds() - lastSeek > 200
                                        ) {
                                            player?.seekTo(it.toLong())
                                            lastSeek = Clock.System.now().toEpochMilliseconds()
                                        }
                                    },
                                    onValueChangeFinished = {
                                        player?.seekTo(temp!!.toLong())
                                        if (previousPlaybackState == true)
                                            player?.play()
                                        else if (previousPlaybackState == false) {
                                            player?.pause()
                                        }
                                        previousPlaybackState = null
                                        temp = null
                                    },
                                    valueRange = 0f..(if (isPreview) 200f else playerState.duration?.toFloat()
                                        ?: 0f),
                                    interactionSource = interactionSource,
                                    thumb = {
                                        SliderDefaults.Thumb(
                                            interactionSource = interactionSource,
                                            thumbSize = DpSize(16.dp, 16.dp)
                                        )
                                    },
                                    track = { sliderState ->
                                        SliderDefaults.Track(
                                            sliderState = sliderState,
                                            thumbTrackGapSize = 4.dp,
                                            modifier = Modifier.height(8.dp)
                                        )
                                    },
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = {
                                            rotation = (rotation ?: 0f).minus(90f)
                                        }
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.outline_rotate_right_24),
                                            null
                                        )
                                    }
                                    AnimatedVisibility(
                                        visible = rotation != null,
                                    ) {
                                        IconButton(
                                            onClick = {
                                                onRotate(clip.id, rotation!!)
                                            }
                                        ) {
                                            Icon(painterResource(R.drawable.outline_check_24), null)
                                        }
                                    }

                                    Spacer(Modifier.weight(1f))

                                    if (clipAspectRatio < 4f / 3) {
                                        TextButton(
                                            onClick = onToggleFullscreen,
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = Color.White
                                            )
                                        ) {
                                            AnimatedContent(state.settings.scalingMode) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        when (it) {
                                                            VideoScalingMode.SCALE_TO_FIT -> "Fit"
                                                            VideoScalingMode.SCALE_TO_9_16 -> "9:16"
                                                            VideoScalingMode.SCALE_TO_FILL -> "Fill"
                                                        },
                                                        style = MaterialTheme.typography.labelLarge
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Icon(
                                                        modifier = Modifier.size(24.dp),
                                                        painter = painterResource(
                                                            when (it) {
                                                                VideoScalingMode.SCALE_TO_FIT -> R.drawable.outline_fit_screen_24
                                                                VideoScalingMode.SCALE_TO_9_16 -> R.drawable.sharp_crop_9_16_24
                                                                VideoScalingMode.SCALE_TO_FILL -> R.drawable.outline_fullscreen_24
                                                            }
                                                        ),
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        }
                                    }

                                }
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
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(0.25f),
                        contentColor = Color.White
                    ),
                    onClick = onBack
                ) {
                    Icon(painterResource(R.drawable.baseline_arrow_back_24), null)
                }
            }
        }
    }
}

@Preview
@Composable
private fun ScrollerScreenPreview() {
    ClipKeepTheme() {
        ScrollerScreen(
            state = ScrollerUiState(isLoading = false, clips = Clip.samples),
            clipId = null,
            isPreview = true
        )
    }
}