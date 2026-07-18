package com.elfen.clipkeep.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.model.ClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipPartEntity

@Database(
    entities = [
        ClipEntity::class,
        EditingClipEntity::class,
        EditingClipPartEntity::class
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = [],
)
abstract class ClipDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao
}