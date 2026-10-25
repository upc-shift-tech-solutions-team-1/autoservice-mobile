
package com.torquelab.autoservice.staff

import com.torquelab.autoservice.staff.data.remote.StaffApi
import com.torquelab.autoservice.staff.data.remote.dto.MechanicDto
import com.torquelab.autoservice.staff.data.repository.StaffRepositoryImpl
import com.torquelab.autoservice.workshop.data.remote.TaskApi
import com.torquelab.autoservice.workshop.data.remote.dto.TaskDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class StaffRepositoryTest {

    private lateinit var staffApi: StaffApi
    private lateinit var taskApi: TaskApi
    private lateinit var repository: StaffRepositoryImpl

    @Before
    fun setUp() {
        staffApi = mock(StaffApi::class.java)
        taskApi = mock(TaskApi::class.java)
        repository = StaffRepositoryImpl(staffApi, taskApi)
    }

    private fun mechanic(
        maxCapacity: Int = 3
    ) = MechanicDto(
        id = 10,
        fullName = "Ana Rojas",
        specialty = "Diagnostico",
        email = "ana@example.com",
        maxCapacity = maxCapacity,
        workshopId = "WS-1"
    )

    private fun task(
        id: Int,
        status: String
    ) = TaskDto(
        id = id,
        workOrderId = 100,
        mechanicId = 10,
        description = "Revision del vehiculo",
        status = status,
        priority = "MEDIUM",
        estimatedTime = 60,
        laborPrice = 50.0
    )

    @Test
    fun `mechanic below maximum capacity should be available`() = runTest {
        // Arrange
        whenever(staffApi.getMechanic(10))
            .thenReturn(mechanic(maxCapacity = 3))

        whenever(taskApi.getTasks(mechanicId = 10))
            .thenReturn(
                listOf(
                    task(1, "PENDING"),
                    task(2, "IN_PROGRESS")
                )
            )

        // Act
        val result = repository.getMechanic(10)

        // Assert
        assertTrue(result.isSuccess)

        val mechanic = result.getOrThrow()

        assertEquals(2, mechanic.assignedTasks)
        assertEquals(3, mechanic.maxCapacity)
        assertTrue(mechanic.isAvailable)

        verify(staffApi).getMechanic(10)
        verify(taskApi).getTasks(mechanicId = 10)
    }

    @Test
    fun `mechanic at maximum capacity should not be available`() = runTest {
        // Arrange
        whenever(staffApi.getMechanic(10))
            .thenReturn(mechanic(maxCapacity = 3))

        whenever(taskApi.getTasks(mechanicId = 10))
            .thenReturn(
                listOf(
                    task(1, "PENDING"),
                    task(2, "IN_PROGRESS"),
                    task(3, "PENDING")
                )
            )

        // Act
        val result = repository.getMechanic(10)

        // Assert
        assertTrue(result.isSuccess)

        val mechanic = result.getOrThrow()

        assertEquals(3, mechanic.assignedTasks)
        assertFalse(mechanic.isAvailable)
    }

    @Test
    fun `completed delivered and cancelled tasks should not occupy capacity`() = runTest {
        // Arrange
        whenever(staffApi.getMechanic(10))
            .thenReturn(mechanic(maxCapacity = 3))

        whenever(taskApi.getTasks(mechanicId = 10))
            .thenReturn(
                listOf(
                    task(1, "COMPLETED"),
                    task(2, "DELIVERED"),
                    task(3, "CANCELLED"),
                    task(4, "PENDING"),
                    task(5, "IN_PROGRESS")
                )
            )

        // Act
        val result = repository.getMechanic(10)

        // Assert
        assertTrue(result.isSuccess)

        val mechanic = result.getOrThrow()

        assertEquals(2, mechanic.assignedTasks)
        assertTrue(mechanic.isAvailable)
    }

    @Test
    fun `task service failure should be returned as repository failure`() = runTest {
        // Arrange
        whenever(staffApi.getMechanic(10))
            .thenReturn(mechanic())

        whenever(taskApi.getTasks(mechanicId = 10))
            .thenThrow(
                IllegalStateException("No se pudieron consultar las tareas")
            )

        // Act
        val result = repository.getMechanic(10)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(
            "No se pudieron consultar las tareas",
            result.exceptionOrNull()?.message
        )
    }
}
