package com.elfen.clipkeep.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.elfen.clipkeep.domain.model.MediaMetadata
import com.elfen.clipkeep.domain.model.VideoError
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun MediaMetadataRetriever.getMetadata(context: Context): MediaMetadata {
    val width =
        this.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
    val height =
        this.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
    val rotation =
        this.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)!!.toInt()
    val isPortrait = rotation == 90 || rotation == 270

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
    )
}
