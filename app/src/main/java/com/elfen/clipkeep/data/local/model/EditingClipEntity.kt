package com.elfen.clipkeep.data.local.model

import androidx.core.net.toUri
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.EditingClipPart

@Entity("editing_clip")
data class EditingClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "NULL")
    val title: String?,
    val mimeType: String,
    val extension: String,
    val fileName: String,
    val uri: String,
    val size: Long,
    val duration: Long,
    val width: Int,
    val height: Int,
    val thumbnailUri: String
)

fun EditingClipEntity.asAppModel(parts: List<EditingClipPart>) = EditingClip(
    id = id,
    title = title,
    mimeType = mimeType,
    extension = extension,
    fileName = fileName,
    uri = uri.toUri(),
    parts = parts,
)