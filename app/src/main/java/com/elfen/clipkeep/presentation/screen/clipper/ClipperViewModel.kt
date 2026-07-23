package com.elfen.clipkeep.presentation.screen.clipper

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Transformer
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.model.ClipEntity
import com.elfen.clipkeep.domain.model.EditingClipPart
import com.elfen.clipkeep.domain.repository.EditRepository
import com.elfen.clipkeep.utils.getFileName
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.io.File
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Clock

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

            _state.update {
                it.copy(
                    exoPlayer = exoPlayer,
                )
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun handleUiEvent(uiEvent: ClipperUiEvent) {
        when (uiEvent) {
            is ClipperUiEvent.AddClip -> {
                viewModelScope.launch {
                    editRepository.addClipping(
                        editId = editId,
                        start = uiEvent.start,
                        end = uiEvent.start + 1_000
                    )
                }
            }

            is ClipperUiEvent.SetClipStartTime -> {
                viewModelScope.launch {
                    editRepository.editClippingStart(uiEvent.id, uiEvent.time)
                }
            }

            is ClipperUiEvent.SetClipEndTime -> {
                viewModelScope.launch {
                    editRepository.editClippingEnd(uiEvent.id, uiEvent.time)
                }
            }

            ClipperUiEvent.Render -> {
                _state.update { it.copy(isRendering = true) }
                viewModelScope.launch {
                    editRepository.confirm(id = editId)
                    _state.update { it.copy(isRendering = false) }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(editId: Long): ClipperViewModel
    }
}