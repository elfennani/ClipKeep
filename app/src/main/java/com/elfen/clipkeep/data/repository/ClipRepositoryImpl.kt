package com.elfen.clipkeep.data.repository

import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.model.asAppModel
import com.elfen.clipkeep.domain.model.Clip
import com.elfen.clipkeep.domain.repository.ClipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClipRepositoryImpl @Inject constructor(
    private val clipDao: ClipDao
) : ClipRepository {
    override fun getClips(): Flow<List<Clip>> {
        return clipDao.queryClipsFlow().map { list -> list.map { it.asAppModel() } }
    }

    override suspend fun deleteClip(id: Long) {
        clipDao.deleteById(id)
    }

    override suspend fun renameClip(id: Long, name: String?) {
        clipDao.updateTitle(id, name)
    }

    override suspend fun rotateClip(id: Long, rotation: Float) {
        clipDao.updateRotation(id, rotation)
    }
}