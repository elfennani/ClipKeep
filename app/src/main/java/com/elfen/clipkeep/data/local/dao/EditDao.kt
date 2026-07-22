package com.elfen.clipkeep.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.elfen.clipkeep.data.local.model.EditingClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipPartEntity
import com.elfen.clipkeep.data.local.relations.EditWithParts
import kotlinx.coroutines.flow.Flow

@Dao
interface EditDao {
    @Insert
    suspend fun insertEdit(edit: EditingClipEntity): Long

    @Query("SELECT * FROM editing_clip WHERE id=:id")
    suspend fun queryEditById(id: Long): EditingClipEntity?

    @Query("SELECT * FROM editing_clip WHERE id=:id")
    fun queryEditByIdFlow(id: Long): Flow<EditWithParts>

    @Query("SELECT * FROM editing_clip")
    fun queryEditsFlow(): Flow<List<EditWithParts>>

    @Transaction
    @Query("SELECT * FROM editing_clip WHERE id=:id")
    suspend fun queryEditWithPartsById(id: Long): EditWithParts?

    @Transaction
    suspend fun insertAndQueryEdit(edit: EditingClipEntity): EditingClipEntity? {
        val id = insertEdit(edit)
        return queryEditById(id)
    }

    @Query("UPDATE editing_clip SET title=:title WHERE id = :id")
    suspend fun updateTitleById(id: Long, title: String?)

    @Insert
    suspend fun insertPart(part: EditingClipPartEntity): Long

    @Query("SELECT * FROM editing_clip_part WHERE id=:id")
    suspend fun queryPartById(id: Long): EditingClipPartEntity

    @Transaction
    suspend fun insertAndQueryPart(part: EditingClipPartEntity): EditingClipPartEntity {
        val id = insertPart(part)
        return queryPartById(id)
    }

    @Update
    suspend fun updatePart(part: EditingClipPartEntity)

    @Query("DELETE FROM editing_clip_part WHERE id=:id")
    suspend fun deletePartById(id: Long)
}