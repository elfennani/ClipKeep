package com.elfen.clipkeep.domain.model

import android.net.Uri
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Clip(
    val id: Long,
    val title: String?,
    val width: Int,
    val height: Int,
    val source: Uri,
    val thumbnail: Uri,
    val uri: Uri,
    val duration: Long,
    val createdAt: Instant,
    val size: Long
) {
    companion object {
        val samples: List<Clip> = listOf(
            Clip(
                id = 1,
                title = "Show moment favorite",
                width = 1920,
                height = 1080,
                source = Uri.EMPTY,
                thumbnail = Uri.EMPTY,
                uri = Uri.EMPTY,
                duration = 10_000,
                size = 1_000_000,
                createdAt = Instant.fromEpochMilliseconds(
                    Clock.System.now().toEpochMilliseconds() - 10_000
                )
            ),
            Clip(
                id = 2,
                title = null,
                width = 1080,
                height = 1920,
                source = Uri.EMPTY,
                thumbnail = Uri.EMPTY,
                uri = Uri.EMPTY,
                duration = 7_500,
                size = 1_000_000,
                createdAt = Instant.fromEpochMilliseconds(
                    Clock.System.now().toEpochMilliseconds() - 97_929_000
                )
            ),
            Clip(
                id = 3,
                title = "Music clip",
                width = 1080,
                height = 1080,
                source = Uri.EMPTY,
                thumbnail = Uri.EMPTY,
                uri = Uri.EMPTY,
                duration = 129_800,
                size = 1_000_000,
                createdAt = Instant.fromEpochMilliseconds(
                    Clock.System.now().toEpochMilliseconds() - 48_960_000
                )
            )
        )
    }
}
