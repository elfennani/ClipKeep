package com.elfen.clipkeep.presentation.screen.scroller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfen.clipkeep.data.local.DataStorePreferences
import com.elfen.clipkeep.domain.model.Settings
import com.elfen.clipkeep.domain.model.settings
import com.elfen.clipkeep.domain.repository.ClipRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ScrollerViewModel.Factory::class)
class ScrollerViewModel @AssistedInject constructor(
    @Assisted private val clipId: Long?,
    private val clipRepository: ClipRepository,
    private val dataStore: DataStorePreferences
) : ViewModel() {
    val settings = dataStore.settings

    val state = combine(settings, clipRepository.getClips()) { settings, clips ->
        ScrollerUiState(isLoading = false, clips = clips, fullscreen = settings.fullscreen)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ScrollerUiState()
    )

    fun rotate(clipId: Long, rotation: Float) {
        viewModelScope.launch {
            clipRepository.rotateClip(clipId, rotation)
        }
    }

    fun toggleFullscreen() {
        viewModelScope.launch {
            Settings.setFullscreen(dataStore, !state.value.fullscreen)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(clipId: Long?): ScrollerViewModel
    }
}