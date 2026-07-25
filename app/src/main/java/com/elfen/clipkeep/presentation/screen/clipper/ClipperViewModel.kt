package com.elfen.clipkeep.presentation.screen.clipper

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.animation.core.RepeatMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.elfen.clipkeep.domain.repository.EditRepository
import com.elfen.clipkeep.presentation.state.PlayerState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

private const val TAG = "ClipperViewModel"

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@HiltViewModel(assistedFactory = ClipperViewModel.Factory::class)
class ClipperViewModel @AssistedInject constructor(
    @Assisted private val editId: Long,
    @ApplicationContext private val context: Context,
    private val editRepository: EditRepository
) : ViewModel() {
    private val edit = editRepository.getEditById(editId)
    private val _state = MutableStateFlow(ClipperUiState())
    val state: StateFlow<ClipperUiState> = combine(_state, edit) { state, edit ->
        state.copy(
            isLoading = false,
            clip = edit
        )
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, ClipperUiState())

    init {
        viewModelScope.launch {
            val clip = state.first { it.clip != null }.clip!!
            val exoPlayer = ExoPlayer.Builder(context).build();
            val media = MediaItem.fromUri(clip.uri);
            exoPlayer.playWhenReady = true
            exoPlayer.addMediaItem(media)
            exoPlayer.prepare()
            exoPlayer.repeatMode = REPEAT_MODE_ONE

            _state.update {
                it.copy(
                    exoPlayer = exoPlayer,
                )
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun handleUiEvent(event: ClipperUiEvent) {
        when (event) {
            is ClipperUiEvent.AddClip -> {
                viewModelScope.launch {
                    editRepository.addClipping(
                        editId = editId,
                        start = event.start,
                        end = min(event.start + 10_000, state.value.clip!!.duration)
                    )
                }
            }

            is ClipperUiEvent.SetClipStartTime -> {
                viewModelScope.launch {
                    editRepository.editClippingStart(event.id, event.time)
                }
            }

            is ClipperUiEvent.SetClipEndTime -> {
                viewModelScope.launch {
                    editRepository.editClippingEnd(event.id, event.time)
                }
            }

            ClipperUiEvent.Render -> {
                _state.update { it.copy(isRendering = true) }
                viewModelScope.launch {
                    editRepository.confirm(id = editId)
                    _state.update { it.copy(isRendering = false) }
                }
            }

            is ClipperUiEvent.TogglePart -> {
                viewModelScope.launch {
                    editRepository.toggleClipping(event.id)
                }
            }

            is ClipperUiEvent.UpdatePartName -> {
                viewModelScope.launch {
                    editRepository.renameClipping(event.id, event.name)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        state.value.exoPlayer?.release()
    }

    @AssistedFactory
    interface Factory {
        fun create(editId: Long): ClipperViewModel
    }
}