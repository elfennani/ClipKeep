package com.elfen.clipkeep.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
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
    @Provides
    @Singleton
    fun database(
        @ApplicationContext context: Context
    ): ClipDatabase {
        return Room
            .databaseBuilder(context, ClipDatabase::class.java, "app.db")
            .build()
    }

    @Provides
    @Singleton
    fun clipDao(db: ClipDatabase): ClipDao = db.clipDao()

    @Provides
    @Singleton
    fun editDao(db: ClipDatabase) = db.editDao()
}