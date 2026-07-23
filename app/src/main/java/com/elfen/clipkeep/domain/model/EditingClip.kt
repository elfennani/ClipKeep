package com.elfen.clipkeep.domain.model

import android.net.Uri
import androidx.core.net.toUri

data class EditingClip(
    val id: Long,
    val title: String?,
    val mimeType: String,
    val extension: String,
    val fileName: String,
    val uri: Uri,
    val parts: List<EditingClipPart>
) {
    companion object {
        val samples: List<EditingClip> = listOf(
            EditingClip(
                id = 1L,
                title = "Summer Vacation",
                mimeType = "video/mp4",
                extension = "mp4",
                fileName = "summer_vacation.mp4",
                uri = "content://media/external/video/media/1".toUri(),
                parts = EditingClipPart.samples
            ),
            EditingClip(
                id = 2L,
                title = "Meeting Recording",
                mimeType = "audio/mpeg",
                extension = "mp3",
                fileName = "meeting_recording.mp3",
                uri = "content://media/external/audio/media/2".toUri(),
                parts = EditingClipPart.samples
            ),
            EditingClip(
                id = 3L,
                title = null,
                mimeType = "video/avi",
                extension = "avi",
                fileName = "clip_avi.avi",
                uri = "file:///storage/emulated/0/Movies/clip_avi.avi".toUri(),
                parts = EditingClipPart.samples
            ),
            EditingClip(
                id = 4L,
                title = "Birthday Highlights",
                mimeType = "video/mkv",
                extension = "mkv",
                fileName = "birthday_highlights.mkv",
                uri = "content://media/external/video/media/4".toUri(),
                parts = EditingClipPart.samples
            ),
            EditingClip(
                id = 5L,
                title = "Podcast Episode 10",
                mimeType = "audio/wav",
                extension = "wav",
                fileName = "podcast_ep10.wav",
                uri = "file:///storage/emulated/0/Podcasts/podcast_ep10.wav".toUri(),
                parts = EditingClipPart.samples
            )
        )
    }
}