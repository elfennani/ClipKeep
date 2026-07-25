package com.elfen.clipkeep.presentation.screen.edit_part

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.exoplayer.ExoPlayer
import com.elfen.clipkeep.domain.repository.EditRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditPartViewModel.Factory::class)
class EditPartViewModel @AssistedInject constructor(
    @Assisted private val route: EditPartRoute,
    @ApplicationContext private val context: Context,
    private val editRepository: EditRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EditPartUiState())
    private val edit = editRepository.getEditById(route.editId)

    val state = combine(_state, edit) { state, edit ->
        val part = edit.parts.first { it.id == route.partId }
        state.copy(
            isLoading = false,
            edit = edit,
            part = part,
            startMs = if (state.startMs == 0L && state.endMs == 0L) part.startMs else state.startMs,
            endMs = if (state.startMs == 0L && state.endMs == 0L) part.finishMs else state.endMs,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _state.value)

    init {
        viewModelScope.launch {
            val clip = state.first { it.edit != null }.edit!!
            val exoPlayer = ExoPlayer.Builder(context).build();
            val media = MediaItem.fromUri(clip.uri).buildUpon().setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(state.value.startMs)
                    .setEndPositionMs(state.value.endMs)
                    .build()
            ).build();
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

    fun updateRange(startMs: Long, endMs: Long) {
        Log.d("updateRange", "startMs: $startMs, endMs: $endMs")
        _state.update { it.copy(startMs = startMs, endMs = endMs) }
    }

    fun setPlayerClipping(shouldClip: Boolean) {
        val startMs = state.value.startMs
        val endMs = state.value.endMs
        val exoPlayer = state.value.exoPlayer ?: return;

        val currentPosition = exoPlayer.currentPosition

        val updatedMediaItem = exoPlayer.currentMediaItem!!
            .buildUpon()
            .setClippingConfiguration(
                if (!shouldClip)
                    MediaItem.ClippingConfiguration.UNSET
                else
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(startMs)
                        .setEndPositionMs(endMs)
                        .build()
            )
            .build()

        exoPlayer.setMediaItem(
            updatedMediaItem,
            currentPosition
        )
        _state.update { it.copy(isClipped = shouldClip) }

        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun confirm(onDismiss: () -> Unit = {}) {
        viewModelScope.launch {
            editRepository.editClippingStart(route.partId, state.value.startMs)
            editRepository.editClippingEnd(route.partId, state.value.endMs)
            onDismiss()
        }
    }

    override fun onCleared() {
        super.onCleared()
        state.value.exoPlayer?.release()
    }

    @AssistedFactory
    interface Factory {
        fun create(route: EditPartRoute): EditPartViewModel
    }
}