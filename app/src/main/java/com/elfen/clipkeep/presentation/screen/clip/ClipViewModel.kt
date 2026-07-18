package com.elfen.clipkeep.presentation.screen.clip

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.model.asAppModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ClipViewModel.Factory::class)
class ClipViewModel @AssistedInject constructor(
    @Assisted private val id: Int,
    private val clipDao: ClipDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(ClipUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val clip = clipDao.queryClip(id.toLong())!!.asAppModel()
            val exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer.playWhenReady = true
            exoPlayer.setMediaItem(
                MediaItem.fromUri(clip.uri)
            )

            _state.update {
                it.copy(isLoading = false, exoPlayer = exoPlayer)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(id: Int): ClipViewModel
    }

    override fun onCleared() {
        state.value.exoPlayer?.release()
    }
}