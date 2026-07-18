package com.elfen.clipkeep.data.local.model

import android.net.Uri
import androidx.core.net.toUri
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.EditingClipPart

@Entity("editing_clip")
data class EditingClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = -1,
    val mimeType: String,
    val extension: String,
    val fileName: String,
    val uri: String,
)

fun EditingClipEntity.asAppModel(parts: List<EditingClipPart>) = EditingClip(
    id = id,
    mimeType = mimeType,
    extension = extension,
    fileName = fileName,
    uri = uri.toUri(),
    parts = parts
)