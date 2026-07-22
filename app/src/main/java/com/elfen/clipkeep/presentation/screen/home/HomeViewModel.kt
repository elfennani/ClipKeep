package com.elfen.clipkeep.presentation.screen.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.model.ClipEntity
import com.elfen.clipkeep.data.local.model.asAppModel
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.repository.ClipRepository
import com.elfen.clipkeep.domain.repository.EditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val clipRepository: ClipRepository,
    private val editRepository: EditRepository
) : ViewModel() {
    val state = clipRepository.getClips().map {
        HomeUiState(isLoading = false, clips = it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, HomeUiState())

    fun createEdit(uri: Uri, onCreate: (EditingClip) -> Unit) {
        viewModelScope.launch {
            editRepository.startEdit(uri).also(onCreate)
        }
    }

    fun deleteClip(clipId: Long) {
        viewModelScope.launch {
            clipRepository.deleteClip(clipId)
        }
    }
}