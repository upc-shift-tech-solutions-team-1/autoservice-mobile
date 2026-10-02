package com.torquelab.autoservice.fleet.di

import com.torquelab.autoservice.fleet.data.RemoteFleetRepository
import com.torquelab.autoservice.fleet.domain.FleetRepository
import com.torquelab.autoservice.fleet.domain.usecase.RegisterVehicleUseCase
import com.torquelab.autoservice.fleet.domain.usecase.RegisterVehicleUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FleetModule {

    @Binds
    @Singleton
    abstract fun bindFleetRepository(
        impl: RemoteFleetRepository
    ): FleetRepository

    @Binds
    @Singleton
    abstract fun bindRegisterVehicleUseCase(
        impl: RegisterVehicleUseCaseImpl
    ): RegisterVehicleUseCase
}
