package com.elfen.clipkeep.domain.model

data class EditingClipPart(
    val id: Int,
    val name: String,
    val startMs: Long,
    val finishMs: Long,
    val enabled: Boolean = true
)
