package com.elfen.clipkeep.data.local.model

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.elfen.clipkeep.domain.model.Crop
import com.elfen.clipkeep.domain.model.EditingClipPart

@Entity("editing_clip_part")
data class EditingClipPartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String?,
    val startMs: Long,
    val finishMs: Long,
    val enabled: Boolean,
    val editId: Long,
    @Embedded
    val crop: Crop
)

fun EditingClipPartEntity.asAppModel() = EditingClipPart(
    id = id,
    name = name,
    startMs = startMs,
    finishMs = finishMs,
    enabled = enabled,
    crop = crop
)