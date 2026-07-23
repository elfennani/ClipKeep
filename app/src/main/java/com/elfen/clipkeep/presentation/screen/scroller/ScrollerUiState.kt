package com.elfen.clipkeep.presentation.screen.scroller

import com.elfen.clipkeep.domain.model.Clip

data class ScrollerUiState(
    val isLoading: Boolean = true,
    val clips: List<Clip> = emptyList()
)
