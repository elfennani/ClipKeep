package com.elfen.clipkeep.presentation.screen.clipper

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


@Serializable
data class ClipperRoute(
    val id: Long
) : NavKey