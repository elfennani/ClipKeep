package com.elfen.clipkeep.presentation.screen.home

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import com.elfen.clipkeep.domain.model.Clip
import com.elfen.clipkeep.domain.model.Settings

data class HomeUiState(
    val isLoading: Boolean = true,
    val clips: List<Clip> = emptyList(),
    val settings: Settings = Settings(),
    val listState: LazyStaggeredGridState
)
