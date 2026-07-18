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
import com.elfen.clipkeep.utils.getFileName
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    @Assisted private val uri: Uri,
    @ApplicationContext private val context: Context,
    private val clipDao: ClipDao
) : ViewModel() {
    private val _state = MutableStateFlow(ClipperUiState())
    val state: StateFlow<ClipperUiState> = _state.asStateFlow()

    init {
        val exoPlayer = ExoPlayer.Builder(context).build();
        val media = MediaItem.fromUri(uri);
        exoPlayer.addMediaItem(media)

        _state.update {
            it.copy(
                isLoading = false,
                exoPlayer = exoPlayer,
                uri = uri
            )
        }
    }

    @OptIn(UnstableApi::class)
    fun handleUiEvent(uiEvent: ClipperUiEvent) {
        when (uiEvent) {
            is ClipperUiEvent.AddClip -> {
                _state.update { state ->
                    state.copy(
                        clips = state.clips + EditingClipPart(
                            id = Random.nextInt(),
                            name = "Clip",
                            startMs = uiEvent.start,
                            finishMs = uiEvent.start + 1_000,
                            enabled = true,
                        )
                    )
                }
            }

            is ClipperUiEvent.SetClipStartTime -> {
                _state.update { state ->
                    state.copy(
                        clips = state.clips.map { clip ->
                            if (clip.id == uiEvent.id)
                                clip.copy(
                                    startMs = uiEvent.time
                                )
                            else
                                clip
                        }
                    )
                }
            }

            is ClipperUiEvent.SetClipEndTime -> {
                _state.update { state ->
                    state.copy(
                        clips = state.clips.map { clip ->
                            if (clip.id == uiEvent.id)
                                clip.copy(
                                    finishMs = uiEvent.time
                                )
                            else
                                clip
                        }
                    )
                }
            }

            ClipperUiEvent.Render -> {
                _state.update { it.copy(isRendering = true) }
                val clips = _state.value.clips.map {
                    it to render(it)
                }

                viewModelScope.launch {
                    clips.forEach { (clip, uri) ->
                        clipDao.insertClip(
                            ClipEntity(
                                id = null,
                                uri = uri.toString(),
                                thumbnailUri = Uri.EMPTY.toString(),
                                width = 1920,
                                height = 1080,
                                durationMs = clip.finishMs - clip.startMs,
                                title = null,
                                source = state.value.uri!!.toString(),
                                createdAt = Clock.System.now().toEpochMilliseconds()
                            )
                        )
                    }

                    Log.d(TAG, "handleUiEvent: Clips Rendered!")
                    _state.update { it.copy(isRendering = false) }
                }
            }
        }
    }

    @UnstableApi
    private fun render(clip: EditingClipPart): Uri {
        Log.d("ClipperViewModel", "Starting");
        val inputMediaItem =
            MediaItem.Builder()
                .setUri(uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clip.startMs)
                        .setEndPositionMs(clip.finishMs)
                        .build()
                )
                .build()
        val transformer =
            Transformer.Builder(context)
                .build();
        val outputFile =
            File(context.filesDir, context.getFileName(uri))

        transformer.start(inputMediaItem, outputFile.absolutePath)
        Log.d("ClipperViewModel", "Finished");

        return outputFile.toUri()
    }

    @AssistedFactory
    interface Factory {
        fun create(uri: Uri): ClipperViewModel
    }
}