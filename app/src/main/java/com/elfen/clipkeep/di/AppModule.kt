package com.elfen.clipkeep.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL
import com.elfen.clipkeep.data.local.ClipDatabase
import com.elfen.clipkeep.data.local.dao.ClipDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override suspend fun migrate(db: SQLiteConnection) {
            db.execSQL("DELETE FROM editing_clip_part")

            db.execSQL(
                """
            ALTER TABLE editing_clip_part
            ADD COLUMN x REAL NOT NULL DEFAULT 0
            """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE editing_clip_part
            ADD COLUMN y REAL NOT NULL DEFAULT 0
            """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE editing_clip_part
            ADD COLUMN width REAL NOT NULL DEFAULT 0
            """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE editing_clip_part
            ADD COLUMN height REAL NOT NULL DEFAULT 0
            """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE editing_clip_part
            ADD COLUMN aspectRatioLocked INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun database(
        @ApplicationContext context: Context
    ): ClipDatabase {
        return Room
            .databaseBuilder(context, ClipDatabase::class.java, "app.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun clipDao(db: ClipDatabase): ClipDao = db.clipDao()

    @Provides
    @Singleton
    fun editDao(db: ClipDatabase) = db.editDao()
}