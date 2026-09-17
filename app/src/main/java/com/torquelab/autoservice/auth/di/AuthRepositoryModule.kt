package com.torquelab.autoservice.auth.di

import com.torquelab.autoservice.auth.data.repository.AuthRepositoryImpl
import com.torquelab.autoservice.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: AuthRepositoryImpl
    ): AuthRepository
}