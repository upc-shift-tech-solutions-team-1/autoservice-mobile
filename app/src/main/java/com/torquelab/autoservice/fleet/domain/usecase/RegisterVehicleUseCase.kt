package com.torquelab.autoservice.fleet.domain.usecase

import com.torquelab.autoservice.fleet.domain.FleetRepository
import com.torquelab.autoservice.fleet.domain.Vehicle
import com.torquelab.autoservice.fleet.domain.validate
import java.time.Year
import javax.inject.Inject

interface RegisterVehicleUseCase {
    suspend operator fun invoke(vehicle: Vehicle): Result<Vehicle>
}

class RegisterVehicleUseCaseImpl @Inject constructor(
    private val repository: FleetRepository
) : RegisterVehicleUseCase {
    override suspend operator fun invoke(vehicle: Vehicle): Result<Vehicle> = runCatching {
        val currentYear = Year.now().value
        val validationIssue = vehicle.validate(currentYear)
        if (validationIssue != null) {
            throw IllegalArgumentException("Vehículo inválido: $validationIssue")
        }

        val existingVehicles = repository.vehicles()
        val isDuplicate = existingVehicles.any {
            it.plate.trim().equals(vehicle.plate.trim(), ignoreCase = true) && it.id != vehicle.id
        }
        if (isDuplicate) {
            throw IllegalArgumentException("Ya existe un vehículo registrado con la placa ${vehicle.plate.trim().uppercase()}")
        }

        repository.save(vehicle)
    }
}
