
package com.torquelab.autoservice.fleet.bdd.steps

import com.torquelab.autoservice.fleet.domain.FleetRepository
import com.torquelab.autoservice.fleet.domain.Vehicle
import com.torquelab.autoservice.fleet.domain.VehicleOwner
import com.torquelab.autoservice.fleet.domain.usecase.RegisterVehicleUseCaseImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class VehicleRegistrationSteps {

    private val savedVehicles = mutableListOf<Vehicle>()
    private var vehicleToRegister: Vehicle? = null
    private var registrationResult: Result<Vehicle>? = null
    private var saveCount = 0

    private val repository = object : FleetRepository {
        override suspend fun vehicles(): List<Vehicle> =
            savedVehicles.toList()

        override suspend fun owners(): List<VehicleOwner> =
            emptyList()

        override suspend fun save(vehicle: Vehicle): Vehicle {
            saveCount++
            val saved = vehicle.copy(
                id = if (vehicle.id == 0) savedVehicles.size + 1 else vehicle.id
            )
            savedVehicles.add(saved)
            return saved
        }
    }

    private val useCase = RegisterVehicleUseCaseImpl(repository)

    fun givenNoVehiclesRegistered() {
        savedVehicles.clear()
        saveCount = 0
        registrationResult = null
    }

    fun givenExistingVehicle(plate: String) {
        givenNoVehiclesRegistered()
        savedVehicles.add(validVehicle(plate).copy(id = 1))
    }

    fun givenVehicleToRegister(plate: String) {
        vehicleToRegister = validVehicle(plate)
        registrationResult = null
    }

    suspend fun whenAdministratorRegistersVehicle() {
        val vehicle = requireNotNull(vehicleToRegister)
        registrationResult = useCase(vehicle)
    }

    fun thenRegistrationSucceeds() {
        assertTrue(
            "Vehicle registration should succeed",
            registrationResult?.isSuccess == true
        )
    }

    fun thenVehicleIsSaved(expectedPlate: String) {
        assertEquals(1, savedVehicles.size)
        assertEquals(expectedPlate, savedVehicles.first().plate)
        assertEquals(1, saveCount)
    }

    fun thenRegistrationIsRejectedForDuplicatePlate() {
        val result = requireNotNull(registrationResult)

        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()
                ?.message
                ?.contains("Ya existe") == true
        )
    }

    fun thenNoSecondVehicleIsSaved() {
        assertEquals(1, savedVehicles.size)
        assertEquals(0, saveCount)
    }

    private fun validVehicle(plate: String) = Vehicle(
        plate = plate,
        brand = "Toyota",
        model = "Corolla",
        year = "2022",
        color = "Blanco",
        customerId = 1,
        currentKilometers = 15000
    )
}
