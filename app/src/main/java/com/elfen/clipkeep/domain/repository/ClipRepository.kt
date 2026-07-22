package com.elfen.clipkeep.domain.repository

import com.elfen.clipkeep.domain.model.Clip
import kotlinx.coroutines.flow.Flow

interface ClipRepository {
    fun getClips(): Flow<List<Clip>>
    suspend fun deleteClip(id: Long)
    suspend fun renameClip(id: Long, name: String?)
}