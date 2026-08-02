package com.elfen.clipkeep.data.repository

import android.content.Context
import androidx.core.net.toFile
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import com.elfen.clipkeep.data.local.DataStorePreferences
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.model.asAppModel
import com.elfen.clipkeep.data.services.RotateService
import com.elfen.clipkeep.domain.model.Clip
import com.elfen.clipkeep.domain.model.Settings
import com.elfen.clipkeep.domain.model.settings
import com.elfen.clipkeep.domain.repository.ClipRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClipRepositoryImpl @Inject constructor(
    private val clipDao: ClipDao,
    @ApplicationContext private val context: Context,
    private val dataStore: DataStorePreferences
) : ClipRepository {
    override fun getClips(): Flow<List<Clip>> {
        return combine(clipDao.queryClipsFlow(), dataStore.settings) { list, settings ->
            list.let {
                if (settings.isRandomized) {
                    it.sortedBy { clip -> clip.random }
                } else it
            }.map { it.asAppModel() }
        }
    }

    override suspend fun deleteClip(id: Long) {
        val clip = clipDao.queryClip(id)!!.asAppModel()
        clip.uri.toFile().delete()

        clipDao.deleteById(id)
    }

    override suspend fun renameClip(id: Long, name: String?) {
        clipDao.updateTitle(id, name)
    }

    override suspend fun rotateClip(id: Long, rotation: Float) {
        RotateService.start(context, id, rotation)
    }

    override suspend fun toggleRandomization() {
        Settings.toggleRandomization(dataStore)
    }

    override suspend fun randomizeClips() {
        clipDao.randomizeClips()
    }

    override suspend fun setClipStartMoment(id: Long, moment: Long) {
        clipDao.updateStartMoment(id, moment)
    }
}