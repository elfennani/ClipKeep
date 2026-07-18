package com.elfen.clipkeep.presentation.screen.clipper

import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer
import com.elfen.clipkeep.domain.model.EditingClipPart

data class ClipperUiState(
    val isLoading: Boolean = true,
    val uri: Uri? = null,
    val exoPlayer: ExoPlayer? = null,
    val clips: List<EditingClipPart> = emptyList(),
    val isRendering: Boolean = false
)