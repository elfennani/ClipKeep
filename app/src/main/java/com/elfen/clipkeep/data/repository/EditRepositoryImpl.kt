package com.elfen.clipkeep.data.repository

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.dao.EditDao
import com.elfen.clipkeep.data.local.model.ClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipPartEntity
import com.elfen.clipkeep.data.local.model.asAppModel
import com.elfen.clipkeep.data.local.relations.asAppModel
import com.elfen.clipkeep.domain.model.AppError
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.EditingClipPart
import com.elfen.clipkeep.domain.model.VideoError
import com.elfen.clipkeep.domain.repository.EditRepository
import com.elfen.clipkeep.utils.getFileName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.internal.wait
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.text.toInt
import kotlin.time.Clock
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
        val (width, height, duration, thumbnail) = uri.getMediaMetadata()
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

    private data class MediaMetadata(
        val width: Int,
        val height: Int,
        val duration: Long,
        val thumbnail: Uri
    )

    @OptIn(ExperimentalUuidApi::class)
    private fun Uri.getMediaMetadata(isContentUri: Boolean = true): MediaMetadata {
        val retriever = MediaMetadataRetriever()

        try {
            return if (isContentUri) {
                val pfd = requireNotNull(
                    context.contentResolver.openFileDescriptor(this, "r")
                )

                pfd.use {
                    retriever.setDataSource(it.fileDescriptor)
                    val metadata = retriever.getMetadata()

                    metadata
                }
            } else {
                retriever.setDataSource(toFile().absolutePath)
                retriever.getMetadata()
            }
        } catch (e: VideoError) {
            e.printStackTrace()
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw VideoError.ReadingFailed
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun MediaMetadataRetriever.getMetadata(): MediaMetadata {
        val width =
            this.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height =
            this.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val rotation =
            this.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)!!.toInt()
        val isPortrait = rotation == 90 || rotation == 270

        Log.d(TAG, "getMetadata: rotation: $rotation")
        val duration =
            this.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong()
                ?: throw VideoError.ReadingFailed

        val bitmap = (this.getFrameAtTime(
            (duration / 10) * 1000L,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        ) ?: throw VideoError.ReadingFailed)

        val thumbnailFile = File(context.filesDir, "${Uuid.generateV4()}.jpg")
        thumbnailFile.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
        }

        bitmap.recycle()
        this.release()

        if (width.isNullOrBlank() || height.isNullOrBlank())
            throw VideoError.ReadingFailed

        return MediaMetadata(
            width = if (isPortrait) height.toInt() else width.toInt(),
            height = if (!isPortrait) height.toInt() else width.toInt(),
            duration = duration,
            thumbnail = thumbnailFile.toUri()
        ).also { Log.d(TAG, "getMetadata: $it") }
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
        return editDao.insertAndQueryPart(
            EditingClipPartEntity(
                name = null,
                startMs = start,
                finishMs = end,
                enabled = true,
                editId = editId
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

    override fun getEditById(id: Long): Flow<EditingClip> {
        return editDao.queryEditByIdFlow(id).map { item -> item.asAppModel() }
    }

    override fun getEdits(): Flow<List<EditingClip>> {
        return editDao.queryEditsFlow().map { list -> list.map { item -> item.asAppModel() } }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override suspend fun confirm(id: Long) {
        // FIXME: This function must start up a foreground service instead of doing everything here
        val clip = editDao.queryEditWithPartsById(id)?.asAppModel() ?: throw AppError.NotFound

        clip.parts.forEach { part ->
            val uri = render(clip.uri, part)
            val metadata = uri.getMediaMetadata(isContentUri = false)
            val size = uri.toFile().length()

            clipDao.insertClip(
                ClipEntity(
                    uri = uri.toString(),
                    thumbnailUri = metadata.thumbnail.toString(),
                    width = metadata.width,
                    height = metadata.height,
                    durationMs = metadata.duration,
                    title = part.name,
                    source = clip.uri.toString(),
                    size = size
                )
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @UnstableApi
    private suspend fun render(uri: Uri, clip: EditingClipPart): Uri =
        suspendCancellableCoroutine { continuation ->
            Log.d("ClipperViewModel", "Starting");
            val inputMediaItem =
                MediaItem.Builder()
                    .setUri(uri)
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(clip.startMs)
                            .setEndPositionMs(clip.finishMs)
                            .build()
                    )
                    .build()
            val transformer =
                Transformer.Builder(context)
                    .build();
            val outputFile =
                File(
                    context.filesDir,
                    "${Uuid.generateV4()}.${context.getFileName(uri)!!.substringAfterLast('.')}"
                )

            transformer.addListener(
                object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        super.onCompleted(composition, exportResult)

                        continuation.resume(outputFile.toUri())
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        super.onError(composition, exportResult, exportException)
                        continuation.resumeWithException(exportException)
                    }
                }
            )
            transformer.start(inputMediaItem, outputFile.absolutePath)

            continuation.invokeOnCancellation { transformer.cancel() }
        }
}