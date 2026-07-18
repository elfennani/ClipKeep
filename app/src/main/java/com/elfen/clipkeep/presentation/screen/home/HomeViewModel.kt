package com.elfen.clipkeep.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.model.ClipEntity
import com.elfen.clipkeep.data.local.model.asAppModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val clipDao: ClipDao
) : ViewModel() {
    val state = clipDao.queryClipsFlow().map {
        HomeUiState(isLoading = false, clips = it.map(ClipEntity::asAppModel))
    }.stateIn(viewModelScope, SharingStarted.Lazily, HomeUiState())
}