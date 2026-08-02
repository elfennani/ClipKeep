package com.elfen.clipkeep.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.elfen.clipkeep.data.local.model.ClipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {
    @Insert
    suspend fun insertClip(clip: ClipEntity): Long

    @Query("SELECT * FROM clip ORDER BY id DESC")
    fun queryClipsFlow(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clip WHERE id=:id")
    suspend fun queryClip(id: Long): ClipEntity?

    @Query("DELETE FROM clip WHERE id=:id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE clip SET title=:title WHERE id=:id")
    suspend fun updateTitle(id: Long, title: String?)

    @Query("UPDATE clip SET rotation=:rotation WHERE id=:id")
    suspend fun updateRotation(id: Long, rotation: Float)

    @Query("UPDATE clip SET uri=:uri WHERE id=:id")
    suspend fun updateFile(id: Long, uri: String)

    @Query("UPDATE clip SET thumbnail_uri=:thumbnail, hasRotatedThumbnail=:hasRotated WHERE id=:id")
    suspend fun updateThumbnail(id: Long, thumbnail: String, hasRotated: Boolean = false)

    @Query("UPDATE clip SET random=ABS(RANDOM())")
    suspend fun randomizeClips()

    @Transaction
    suspend fun updateFileAndRotation(id: Long, uri: String, thumbnail: String, rotation: Float) {
        updateFile(id, uri)
        updateThumbnail(id, thumbnail, true)
        updateRotation(id, rotation)
    }
}