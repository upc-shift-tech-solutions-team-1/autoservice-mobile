package com.torquelab.autoservice.customer_management.di

import com.torquelab.autoservice.customer_management.data.remote.CustomerApi
import com.torquelab.autoservice.customer_management.data.repository.CustomerRepositoryImpl
import com.torquelab.autoservice.customer_management.domain.repository.CustomerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CustomerModule {

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        impl: CustomerRepositoryImpl
    ): CustomerRepository

    companion object {
        @Provides
        @Singleton
        fun provideCustomerApi(retrofit: Retrofit): CustomerApi =
            retrofit.create(CustomerApi::class.java)
    }
}
