package com.torquelab.autoservice.fleet.domain.usecase

import com.torquelab.autoservice.fleet.domain.FleetRepository
import com.torquelab.autoservice.fleet.domain.Vehicle
import javax.inject.Inject

class UpdateOdometerUseCase @Inject constructor(
    private val repository: FleetRepository
) {
    suspend operator fun invoke(vehicleId: Int, newKilometers: Int): Result<Vehicle> = runCatching {
        if (newKilometers < 0) {
            throw IllegalArgumentException("El kilometraje no puede ser negativo")
        }
        repository.updateKilometers(vehicleId, newKilometers)
    }
}
