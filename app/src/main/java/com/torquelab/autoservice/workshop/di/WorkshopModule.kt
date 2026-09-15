package com.torquelab.autoservice.workshop.di

import com.torquelab.autoservice.workshop.data.repository.WorkshopRepositoryImpl
import com.torquelab.autoservice.workshop.domain.repository.WorkshopRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkshopModule {

    @Binds
    @Singleton
    abstract fun bindWorkshopRepository(
        implementation: WorkshopRepositoryImpl
    ): WorkshopRepository
}
