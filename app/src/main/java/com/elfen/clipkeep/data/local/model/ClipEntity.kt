package com.elfen.clipkeep.data.local.model

import androidx.core.net.toUri
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.elfen.clipkeep.domain.model.Clip
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity("clip")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    @ColumnInfo(name = "thumbnail_uri")
    val thumbnailUri: String,
    val width: Int,
    val height: Int,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(defaultValue = "NULL")
    val title: String? = null,
    @ColumnInfo(defaultValue = "NULL")
    val source: String? = null,
    val size: Long,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    @ColumnInfo(defaultValue = "0")
    val rotation: Float
)

@OptIn(ExperimentalTime::class)
fun ClipEntity.asAppModel() = Clip(
    id = id,
    title = title,
    width = width,
    height = height,
    source = uri.toUri(),
    thumbnail = thumbnailUri.toUri(),
    uri = uri.toUri(),
    duration = durationMs,
    size = size,
    rotation = rotation,
    createdAt = Instant.fromEpochMilliseconds(createdAt)
)