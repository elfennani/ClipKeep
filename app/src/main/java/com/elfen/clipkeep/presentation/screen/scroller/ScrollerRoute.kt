package com.elfen.clipkeep.presentation.screen.scroller

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ScrollerRoute(
    val clipId: Long? = null
) : NavKey