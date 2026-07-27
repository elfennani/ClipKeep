package com.elfen.clipkeep.data.repository

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Codec
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.dao.EditDao
import com.elfen.clipkeep.data.local.model.ClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipPartEntity
import com.elfen.clipkeep.data.local.model.asAppModel
import com.elfen.clipkeep.data.local.relations.asAppModel
import com.elfen.clipkeep.data.services.RenderService
import com.elfen.clipkeep.domain.model.AppError
import com.elfen.clipkeep.domain.model.Crop
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.EditingClipPart
import com.elfen.clipkeep.domain.model.VideoError
import com.elfen.clipkeep.domain.model.toMedia3Crop
import com.elfen.clipkeep.domain.repository.EditRepository
import com.elfen.clipkeep.utils.getFileName
import com.elfen.clipkeep.utils.getMediaMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.text.toInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "EditRepositoryImpl"

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EditRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val editDao: EditDao,
    private val clipDao: ClipDao
) : EditRepository {
    override suspend fun startEdit(uri: Uri): EditingClip {
        val mimeType = context.contentResolver.getType(uri) ?: throw VideoError.ReadingFailed
        val (width, height, duration, thumbnail) = uri.getMediaMetadata(context)
        val fileName = context.contentResolver.getFileName(uri) ?: throw VideoError.FileNotFound
        val extension = fileName.substringAfterLast('.')
        val size = context.contentResolver.getFileSize(uri) ?: throw VideoError.FileNotFound

        val edit = EditingClipEntity(
            title = null,
            mimeType = mimeType,
            extension = extension,
            fileName = fileName,
            uri = uri.toString(),
            size = size,
            duration = duration,
            width = width,
            height = height,
            thumbnailUri = thumbnail.toString()
        )

        return editDao.insertAndQueryEdit(edit)?.asAppModel(emptyList())
            ?: throw Exception("Failed to Save to Database")
    }

    private fun ContentResolver.getFileName(uri: Uri): String? {
        return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) {
                cursor.getString(index)
            } else
                null
        }
    }

    private fun ContentResolver.getFileSize(uri: Uri): Long? {
        return query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && index >= 0) {
                cursor.getLong(index)
            } else
                null
        }
    }

    override suspend fun renameEdit(
        id: Long,
        name: String?
    ): EditingClip {
        editDao.updateTitleById(id, name)
        return editDao.queryEditWithPartsById(id)?.asAppModel() ?: throw AppError.NotFound
    }

    override suspend fun addClipping(
        editId: Long,
        start: Long,
        end: Long
    ): EditingClipPart {
        val edit = editDao.queryEditById(editId)!!.asAppModel(emptyList())

        return editDao.insertAndQueryPart(
            EditingClipPartEntity(
                name = null,
                startMs = start,
                finishMs = end,
                enabled = true,
                editId = editId,
                crop = Crop(
                    x = 0f,
                    y = 0f,
                    width = edit.width.toFloat(),
                    height = edit.height.toFloat(),
                )
            )
        ).asAppModel()
    }


    override suspend fun renameClipping(id: Long, name: String?) {
        val clipping = editDao.queryPartById(id)
        editDao.updatePart(clipping.copy(name = name))
    }

    override suspend fun deleteClippingById(id: Long) {
        editDao.deletePartById(id)
    }

    override suspend fun toggleClipping(id: Long) {
        val clipping = editDao.queryPartById(id)
        editDao.updatePart(
            clipping.copy(enabled = !clipping.enabled)
        )
    }

    override suspend fun editClippingStart(id: Long, start: Long) {
        val clipping = editDao.queryPartById(id)
        editDao.updatePart(
            clipping.copy(startMs = start)
        )
    }

    override suspend fun editClippingEnd(id: Long, end: Long) {
        val clipping = editDao.queryPartById(id)
        editDao.updatePart(
            clipping.copy(finishMs = end)
        )
    }

    override suspend fun setClippingCrop(
        id: Long,
        crop: Crop
    ) {
        val clipping = editDao.queryPartById(id)

        editDao.updatePart(
            clipping.copy(crop = crop)
        )
    }

    override fun getEditById(id: Long): Flow<EditingClip> {
        return editDao.queryEditByIdFlow(id).map { item -> item.asAppModel() }
    }

    override fun getEdits(): Flow<List<EditingClip>> {
        return editDao.queryEditsFlow().map { list -> list.map { item -> item.asAppModel() } }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override suspend fun confirm(id: Long) {
        val intent = Intent(context, RenderService::class.java)
        intent.action = "RENDER"
        intent.putExtra("edit_id", id)
        context.startForegroundService(intent)
    }
}