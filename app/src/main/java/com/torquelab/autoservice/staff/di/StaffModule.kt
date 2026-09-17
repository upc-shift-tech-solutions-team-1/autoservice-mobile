package com.torquelab.autoservice.staff.di

import com.torquelab.autoservice.staff.data.repository.StaffRepositoryImpl
import com.torquelab.autoservice.staff.domain.repository.StaffRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StaffModule {

    @Binds
    @Singleton
    abstract fun bindStaffRepository(
        implementation: StaffRepositoryImpl
    ): StaffRepository
}
