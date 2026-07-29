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
    val parts: List<EditingClipPart>,
    val size: Long,
    val duration: Long,
    val width: Int,
    val height: Int,
    val thumbnailUri: Uri
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
                parts = EditingClipPart.samples,
                size = 245_000_000L,
                duration = 128_000L,
                width = 1920,
                height = 1080,
                thumbnailUri = "content://media/external/video/media/1/thumbnail".toUri()
            ),
            EditingClip(
                id = 2L,
                title = "City Walk",
                mimeType = "video/mp4",
                extension = "mp4",
                fileName = "city_walk.mp4",
                uri = "content://media/external/video/media/2".toUri(),
                parts = EditingClipPart.samples,
                size = 180_000_000L,
                duration = 2_700_000L,
                width = 2160,
                height = 3840,
                thumbnailUri = "content://media/external/video/media/2/thumbnail".toUri()
            ),
            EditingClip(
                id = 3L,
                title = null,
                mimeType = "video/avi",
                extension = "avi",
                fileName = "clip_avi.avi",
                uri = "file:///storage/emulated/0/Movies/clip_avi.avi".toUri(),
                parts = EditingClipPart.samples,
                size = 780_000_000L,
                duration = 1_800_000L,
                width = 1280,
                height = 720,
                thumbnailUri = "file:///storage/emulated/0/Movies/clip_avi_thumbnail.jpg".toUri()
            ),
            EditingClip(
                id = 4L,
                title = "Birthday Highlights",
                mimeType = "video/mkv",
                extension = "mkv",
                fileName = "birthday_highlights.mkv",
                uri = "content://media/external/video/media/4".toUri(),
                parts = EditingClipPart.samples,
                size = 125_000_000L,
                duration = 900_000L,
                width = 1920,
                height = 1080,
                thumbnailUri = "content://media/external/video/media/4/thumbnail".toUri()
            ),
            EditingClip(
                id = 5L,
                title = "Mountain Hike",
                mimeType = "video/webm",
                extension = "webm",
                fileName = "mountain_hike.webm",
                uri = "file:///storage/emulated/0/Movies/mountain_hike.webm".toUri(),
                parts = EditingClipPart.samples,
                size = 320_000_000L,
                duration = 5_400_000L,
                width = 2560,
                height = 1440,
                thumbnailUri = "file:///storage/emulated/0/Movies/mountain_hike_thumbnail.jpg".toUri()
            )
        )
    }
}