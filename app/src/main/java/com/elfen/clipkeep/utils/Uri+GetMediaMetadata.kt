package com.elfen.clipkeep.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toFile
import com.elfen.clipkeep.domain.model.MediaMetadata
import com.elfen.clipkeep.domain.model.VideoError
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun Uri.getMediaMetadata(context: Context, isContentUri: Boolean = true): MediaMetadata {
    val retriever = MediaMetadataRetriever()

    try {
        return if (isContentUri) {
            val pfd = requireNotNull(
                context.contentResolver.openFileDescriptor(this, "r")
            )

            pfd.use {
                retriever.setDataSource(it.fileDescriptor)
                val metadata = retriever.getMetadata(context)

                metadata
            }
        } else {
            retriever.setDataSource(toFile().absolutePath)
            retriever.getMetadata(context)
        }
    } catch (e: VideoError) {
        e.printStackTrace()
        throw e
    } catch (e: Exception) {
        e.printStackTrace()
        throw VideoError.ReadingFailed
    }
}
