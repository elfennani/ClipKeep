package com.elfen.clipkeep.presentation.screen.home

import android.net.Uri
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfen.clipkeep.data.local.DataStorePreferences
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.model.ClipEntity
import com.elfen.clipkeep.data.local.model.asAppModel
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.settings
import com.elfen.clipkeep.domain.repository.ClipRepository
import com.elfen.clipkeep.domain.repository.EditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val clipRepository: ClipRepository,
    private val editRepository: EditRepository,
    private val dataStore: DataStorePreferences
) : ViewModel() {
    val listState = LazyStaggeredGridState()

    val state = combine(dataStore.settings, clipRepository.getClips()) { settings, clips ->
        HomeUiState(isLoading = false, clips = clips, settings = settings, listState = listState)
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, HomeUiState(listState = listState))

    fun createEdit(uri: Uri, onCreate: (EditingClip) -> Unit) {
        viewModelScope.launch {
            editRepository.startEdit(uri).also(onCreate)
        }
    }

    fun toggleRandomization() {
        viewModelScope.launch {
            clipRepository.toggleRandomization()
        }
    }

    fun randomizeClips() {
        viewModelScope.launch {
            clipRepository.randomizeClips()
            listState.scrollToItem(0)
        }
    }

    fun deleteClip(clipId: Long) {
        viewModelScope.launch {
            clipRepository.deleteClip(clipId)
        }
    }
}