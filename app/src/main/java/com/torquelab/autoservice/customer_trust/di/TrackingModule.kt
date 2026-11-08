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
        fun provideTrackingApi(retrofit: Retrofit, client: okhttp3.OkHttpClient): TrackingApi {
            // Public tracking must not send a saved token or trigger an authenticated 401 logout.
            val anonymousClient = client.newBuilder().apply {
                interceptors().removeAll { it is com.torquelab.autoservice.shared.network.AuthInterceptor }
            }.build()
            return retrofit.newBuilder().client(anonymousClient).build().create(TrackingApi::class.java)
        }
    }
}
