package com.torquelab.autoservice.mechanic.di

import com.torquelab.autoservice.mechanic.data.repository.MechanicRepositoryImpl
import com.torquelab.autoservice.mechanic.domain.repository.MechanicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MechanicModule {

    @Binds
    @Singleton
    abstract fun bindMechanicRepository(
        mechanicRepositoryImpl: MechanicRepositoryImpl
    ): MechanicRepository
}
