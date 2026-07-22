package com.elfen.clipkeep.domain.model

import android.net.Uri

data class EditingClip(
    val id: Long,
    val title: String?,
    val mimeType: String,
    val extension: String,
    val fileName: String,
    val uri: Uri,
    val parts: List<EditingClipPart>
)
