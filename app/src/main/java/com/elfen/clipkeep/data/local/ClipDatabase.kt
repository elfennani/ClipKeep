package com.elfen.clipkeep.data.local

import androidx.room3.AutoMigration
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.data.local.dao.EditDao
import com.elfen.clipkeep.data.local.model.ClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipPartEntity

@Database(
    entities = [
        ClipEntity::class,
        EditingClipEntity::class,
        EditingClipPartEntity::class
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(2, 3),
        AutoMigration(3, 4)
    ],
)
abstract class ClipDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao
    abstract fun editDao(): EditDao
}