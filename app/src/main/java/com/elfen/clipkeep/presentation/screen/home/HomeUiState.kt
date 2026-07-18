package com.elfen.clipkeep.presentation.screen.home

import com.elfen.clipkeep.domain.model.Clip

data class HomeUiState(
    val isLoading: Boolean = true,
    val clips: List<Clip> = emptyList()
)
