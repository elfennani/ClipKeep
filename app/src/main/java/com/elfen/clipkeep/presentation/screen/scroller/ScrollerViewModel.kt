package com.elfen.clipkeep.presentation.screen.scroller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfen.clipkeep.domain.repository.ClipRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel(assistedFactory = ScrollerViewModel.Factory::class)
class ScrollerViewModel @AssistedInject constructor(
    @Assisted private val clipId: Long?,
    private val clipRepository: ClipRepository
) : ViewModel() {
    val state = clipRepository.getClips().map {
        ScrollerUiState(isLoading = false, clips = it)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ScrollerUiState()
    )

    @AssistedFactory
    interface Factory {
        fun create(clipId: Long?): ScrollerViewModel
    }
}