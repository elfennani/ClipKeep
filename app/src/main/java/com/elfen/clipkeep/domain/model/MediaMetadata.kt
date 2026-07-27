package com.elfen.clipkeep.domain.model

import android.net.Uri

data class MediaMetadata(
    val width: Int,
    val height: Int,
    val duration: Long,
    val thumbnail: Uri
)