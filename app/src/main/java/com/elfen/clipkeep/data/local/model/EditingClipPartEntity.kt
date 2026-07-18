package com.elfen.clipkeep.data.local.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.elfen.clipkeep.domain.model.EditingClipPart

@Entity("editing_clip_part")
data class EditingClipPartEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val startMs: Long,
    val finishMs: Long,
    val enabled: Boolean
)

fun EditingClipPartEntity.asAppModel() = EditingClipPart(
    id = id,
    name = name,
    startMs = startMs,
    finishMs = finishMs,
    enabled = enabled
)