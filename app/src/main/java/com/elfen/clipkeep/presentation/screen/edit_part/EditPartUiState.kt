package com.elfen.clipkeep.presentation.screen.edit_part

import androidx.compose.runtime.Immutable
import androidx.media3.exoplayer.ExoPlayer
import com.elfen.clipkeep.domain.model.Crop
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.EditingClipPart

@Immutable
data class EditPartUiState(
    val isLoading: Boolean = true,
    val edit: EditingClip? = null,
    val part: EditingClipPart? = null,
    val startMs: Long = 0,
    val endMs: Long = 0,
    val exoPlayer: ExoPlayer? = null,
    val isClipped: Boolean = true,
    val crop: Crop = Crop(
        x = 0f,
        y = 0f,
        width = 1f,
        height = 1f,
        aspectRatioLocked = true
    ),
)
