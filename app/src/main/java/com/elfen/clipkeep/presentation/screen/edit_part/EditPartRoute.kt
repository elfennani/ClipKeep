package com.elfen.clipkeep.presentation.screen.edit_part

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class EditPartRoute(
    val editId: Long,
    val partId: Long
) : NavKey
