
package com.torquelab.autoservice.fleet.bdd

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.torquelab.autoservice.fleet.bdd.steps.VehicleRegistrationSteps
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VehicleRegistrationBddTest {

    private lateinit var steps: VehicleRegistrationSteps

    @Before
    fun setUp() {
        steps = VehicleRegistrationSteps()
    }

    @Test
    fun scenario_registerValidVehicle() = runBlocking {
        // Given
        steps.givenNoVehiclesRegistered()
        steps.givenVehicleToRegister("ABC-123")

        // When
        steps.whenAdministratorRegistersVehicle()

        // Then
        steps.thenRegistrationSucceeds()
        steps.thenVehicleIsSaved("ABC-123")
    }

    @Test
    fun scenario_rejectDuplicateVehiclePlate() = runBlocking {
        // Given
        steps.givenExistingVehicle("ABC-123")
        steps.givenVehicleToRegister(" abc-123 ")

        // When
        steps.whenAdministratorRegistersVehicle()

        // Then
        steps.thenRegistrationIsRejectedForDuplicatePlate()
        steps.thenNoSecondVehicleIsSaved()
    }
}
