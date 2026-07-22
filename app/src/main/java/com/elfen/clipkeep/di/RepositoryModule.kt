package com.elfen.clipkeep.di

import com.elfen.clipkeep.data.repository.ClipRepositoryImpl
import com.elfen.clipkeep.data.repository.EditRepositoryImpl
import com.elfen.clipkeep.domain.repository.ClipRepository
import com.elfen.clipkeep.domain.repository.EditRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindClipRepo(clipRepositoryImpl: ClipRepositoryImpl): ClipRepository

    @Binds
    abstract fun bindEditRepo(editRepositoryImpl: EditRepositoryImpl): EditRepository
}