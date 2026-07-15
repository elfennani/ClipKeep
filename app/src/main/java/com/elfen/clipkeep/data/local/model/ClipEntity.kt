package com.elfen.clipkeep.data.local.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity("clip")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = -1,
    val uri: String,
    @ColumnInfo(name = "thumbnail_uri")
    val thumbnailUri: String,
    val width: Int,
    val height: Int,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long
)