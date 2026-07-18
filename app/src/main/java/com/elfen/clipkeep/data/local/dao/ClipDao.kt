package com.elfen.clipkeep.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.elfen.clipkeep.data.local.model.ClipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {
    @Insert
    suspend fun insertClip(clip: ClipEntity): Long

    @Query("SELECT * FROM clip")
    fun queryClipsFlow(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clip WHERE id=:id")
    suspend fun queryClip(id: Long): ClipEntity?
}