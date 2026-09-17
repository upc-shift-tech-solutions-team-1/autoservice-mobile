package com.torquelab.autoservice.customer_trust.di

import com.torquelab.autoservice.customer_trust.data.remote.TrackingApi
import com.torquelab.autoservice.customer_trust.data.repository.TrackingRepositoryImpl
import com.torquelab.autoservice.customer_trust.domain.repository.TrackingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingModule {

    @Binds
    @Singleton
    abstract fun bindTrackingRepository(
        impl: TrackingRepositoryImpl
    ): TrackingRepository

    companion object {
        @Provides
        @Singleton
        fun provideTrackingApi(retrofit: Retrofit): TrackingApi =
            retrofit.create(TrackingApi::class.java)
    }
}
