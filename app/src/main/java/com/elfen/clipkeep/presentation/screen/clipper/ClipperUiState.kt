package com.elfen.clipkeep.presentation.screen.clipper

import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.EditingClipPart
import com.elfen.clipkeep.presentation.state.PlayerState

data class ClipperUiState(
    val isLoading: Boolean = true,
    val exoPlayer: ExoPlayer? = null,
    val isRendering: Boolean = false,
    val clip: EditingClip? = null
)