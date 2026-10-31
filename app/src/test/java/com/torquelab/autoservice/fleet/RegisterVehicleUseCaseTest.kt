
package com.torquelab.autoservice.fleet

import com.torquelab.autoservice.fleet.domain.FleetRepository
import com.torquelab.autoservice.fleet.domain.Vehicle
import com.torquelab.autoservice.fleet.domain.usecase.RegisterVehicleUseCaseImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RegisterVehicleUseCaseTest {

    private lateinit var repository: FleetRepository
    private lateinit var useCase: RegisterVehicleUseCaseImpl

    @Before
    fun setUp() {
        repository = mock(FleetRepository::class.java)
        useCase = RegisterVehicleUseCaseImpl(repository)
    }

    private fun createValidVehicle() = Vehicle(
        plate = "ABC-123",
        brand = "Toyota",
        model = "Corolla",
        year = "2022",
        color = "White",
        customerId = 1,
        currentKilometers = 15000
    )

    @Test
    fun `register valid vehicle should save successfully`() = runTest {
        // Arrange
        val vehicle = createValidVehicle()

        whenever(repository.vehicles()).thenReturn(emptyList())
        whenever(repository.save(vehicle)).thenReturn(vehicle)

        // Act
        val result = useCase(vehicle)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(vehicle, result.getOrNull())

        verify(repository).save(vehicle)
    }

    @Test
    fun `register duplicate plate should reject vehicle`() = runTest {
        // Arrange
        val existingVehicle = createValidVehicle().copy(
            id = 1,
            plate = "ABC-123"
        )

        val newVehicle = createValidVehicle().copy(
            id = 0,
            plate = " abc-123 "
        )

        whenever(repository.vehicles()).thenReturn(
            listOf(existingVehicle)
        )

        // Act
        val result = useCase(newVehicle)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()
                ?.message
                ?.contains("Ya existe") == true
        )

        verify(repository, never()).save(any())
    }

    @Test
    fun `register invalid vehicle should not save`() = runTest {
        // Arrange
        val invalidVehicle = createValidVehicle().copy(
            plate = ""
        )

        // Act
        val result = useCase(invalidVehicle)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull() is IllegalArgumentException
        )

        assertTrue(
            result.exceptionOrNull()
                ?.message
                ?.contains("PLATE") == true
        )

        verify(repository, never()).save(any())
    }
}
