
package com.torquelab.autoservice.staff.bdd.steps

import com.torquelab.autoservice.staff.data.remote.StaffApi
import com.torquelab.autoservice.staff.data.remote.dto.MechanicDto
import com.torquelab.autoservice.staff.data.repository.StaffRepositoryImpl
import com.torquelab.autoservice.staff.domain.model.Mechanic
import com.torquelab.autoservice.workshop.data.remote.TaskApi
import com.torquelab.autoservice.workshop.data.remote.dto.TaskDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.lang.reflect.Proxy

class StaffAvailabilitySteps {

    private var maxCapacity = 0
    private var activeTasks = 0
    private var result: Result<Mechanic>? = null

    private val staffApi: StaffApi = Proxy.newProxyInstance(
        StaffApi::class.java.classLoader,
        arrayOf(StaffApi::class.java)
    ) { _, method, _ ->
        when (method.name) {
            "getMechanic" -> mechanicDto()
            else -> error("Metodo no esperado: ${method.name}")
        }
    } as StaffApi

    private val taskApi: TaskApi = Proxy.newProxyInstance(
        TaskApi::class.java.classLoader,
        arrayOf(TaskApi::class.java)
    ) { _, method, _ ->
        when (method.name) {
            "getTasks" -> tasks()
            else -> error("Metodo no esperado: ${method.name}")
        }
    } as TaskApi

    private val repository = StaffRepositoryImpl(
        staffApi = staffApi,
        taskApi = taskApi
    )

    fun givenMechanicCapacity(capacity: Int) {
        maxCapacity = capacity
    }

    fun givenActiveTasks(quantity: Int) {
        activeTasks = quantity
    }

    private fun mechanicDto() = MechanicDto(
        id = 10,
        fullName = "Ana Rojas",
        email = "ana@example.com",
        specialty = "Diagnostico",
        maxCapacity = maxCapacity,
        workshopId = "WS-1"
    )

    private fun tasks(): List<TaskDto> =
        (1..activeTasks).map { id ->
            TaskDto(
                id = id,
                workOrderId = 100,
                mechanicId = 10,
                description = "Mantenimiento",
                status = "IN_PROGRESS",
                priority = "MEDIUM",
                estimatedTime = 60,
                laborPrice = 50.0
            )
        }

    suspend fun whenAdministratorChecksAvailability() {
        result = repository.getMechanic(10)
    }

    fun thenMechanicIsAvailable() {
        val mechanic = requireNotNull(result).getOrThrow()
        assertTrue(mechanic.isAvailable)
    }

    fun thenMechanicIsNotAvailable() {
        val mechanic = requireNotNull(result).getOrThrow()
        assertFalse(mechanic.isAvailable)
    }

    fun thenAssignedTasksAre(expected: Int) {
        val mechanic = requireNotNull(result).getOrThrow()
        assertEquals(expected, mechanic.assignedTasks)
    }
}
