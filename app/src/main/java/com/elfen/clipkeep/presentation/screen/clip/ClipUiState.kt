package com.elfen.clipkeep.presentation.screen.clip

import androidx.media3.exoplayer.ExoPlayer

data class ClipUiState(
    val isLoading: Boolean = true,
    val exoPlayer: ExoPlayer? = null,
)
