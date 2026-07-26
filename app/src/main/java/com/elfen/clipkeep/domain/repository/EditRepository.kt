package com.elfen.clipkeep.domain.repository

import android.net.Uri
import com.elfen.clipkeep.domain.model.Crop
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.EditingClipPart
import kotlinx.coroutines.flow.Flow

interface EditRepository {
    suspend fun startEdit(uri: Uri): EditingClip
    suspend fun renameEdit(id: Long, name: String?): EditingClip

    suspend fun addClipping(editId: Long, start: Long, end: Long): EditingClipPart
    suspend fun renameClipping(id: Long, name: String?)
    suspend fun deleteClippingById(id: Long)
    suspend fun toggleClipping(id: Long)
    suspend fun editClippingStart(id: Long, start: Long)
    suspend fun editClippingEnd(id: Long, end: Long)
    suspend fun setClippingCrop(id: Long, crop: Crop)

    fun getEditById(id: Long): Flow<EditingClip>
    fun getEdits(): Flow<List<EditingClip>>

    suspend fun confirm(id: Long)
}