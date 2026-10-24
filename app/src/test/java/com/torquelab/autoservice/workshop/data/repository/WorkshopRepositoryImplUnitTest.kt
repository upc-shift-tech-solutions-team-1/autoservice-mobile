package com.torquelab.autoservice.workshop.data.repository

import com.torquelab.autoservice.customer_management.data.remote.CustomerApi
import com.torquelab.autoservice.customer_management.data.remote.dto.CustomerDto
import com.torquelab.autoservice.fleet.data.FleetApi
import com.torquelab.autoservice.fleet.domain.Vehicle
import com.torquelab.autoservice.staff.data.remote.StaffApi
import com.torquelab.autoservice.staff.data.remote.dto.MechanicDto
import com.torquelab.autoservice.workshop.data.remote.TaskApi
import com.torquelab.autoservice.workshop.data.remote.WorkshopApi
import com.torquelab.autoservice.workshop.data.remote.dto.CreateTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.FinancialSummaryDto
import com.torquelab.autoservice.workshop.data.remote.dto.PatchTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.TaskDto
import com.torquelab.autoservice.workshop.data.remote.dto.UpdateTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.WorkOrderDto
import com.torquelab.autoservice.workshop.domain.model.CreateTaskInput
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.UpdateTaskInput
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class WorkshopRepositoryImplUnitTest {

    private lateinit var workshopApi: WorkshopApi
    private lateinit var taskApi: TaskApi
    private lateinit var customerApi: CustomerApi
    private lateinit var fleetApi: FleetApi
    private lateinit var staffApi: StaffApi
    private lateinit var repository: WorkshopRepositoryImpl

    @Before
    fun setUp() {
        workshopApi = mock(WorkshopApi::class.java)
        taskApi = mock(TaskApi::class.java)
        customerApi = mock(CustomerApi::class.java)
        fleetApi = mock(FleetApi::class.java)
        staffApi = mock(StaffApi::class.java)
        repository = WorkshopRepositoryImpl(workshopApi, taskApi, customerApi, fleetApi, staffApi)
    }

    @Test
    fun `getWorkOrders joins customers, vehicles, mechanics, tasks and computes progress and risk correctly`() = runTest {
        val ordersDto = listOf(
            WorkOrderDto(
                id = 10,
                workshopId = "WS-01",
                trackingCode = "TRK-010",
                vehicleId = 100,
                customerId = 5,
                mechanicId = 1,
                description = "Reparación general de transmisión",
                status = "PENDING",
                price = 1200.0,
                estimatedDate = "2026-10-30",
                startDate = "2026-10-24",
                tasksCompleted = false,
                sparePartsChecked = false,
                diagnosisValidated = false,
                cleaningDone = false,
                finalTestDone = false
            )
        )

        val customers = listOf(
            CustomerDto(id = "5", workshopId = "WS-01", fullName = "Juan Perez", dni = "12345678", email = "juan@example.com", phone = "987654321")
        )
        val vehicles = listOf(
            Vehicle(id = 100, plate = "PE-999", brand = "Honda", model = "Civic")
        )
        val mechanics = listOf(
            MechanicDto(id = 1, fullName = "Carlos Mecanico", specialty = "Transmisiones", email = "carlos@taller.com", maxCapacity = 3, workshopId = "WS-01")
        )
        val tasks = listOf(
            TaskDto(
                id = 501,
                workOrderId = 10,
                mechanicId = 1,
                description = "Desmontaje de caja",
                status = "COMPLETED",
                priority = "HIGH",
                estimatedTime = 120,
                laborPrice = 200.0
            ),
            TaskDto(
                id = 502,
                workOrderId = 10,
                mechanicId = 1,
                description = "Reemplazo de piñón",
                status = "IN_PROGRESS",
                priority = "HIGH",
                estimatedTime = 90,
                laborPrice = 150.0
            )
        )

        whenever(workshopApi.getWorkOrders()).thenReturn(ordersDto)
        whenever(customerApi.getCustomers()).thenReturn(customers)
        whenever(fleetApi.vehicles()).thenReturn(vehicles)
        whenever(staffApi.getMechanics()).thenReturn(mechanics)
        whenever(taskApi.getTasks()).thenReturn(tasks)

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        val list = result.getOrNull()!!
        assertEquals(1, list.size)

        val order = list[0]
        assertEquals("Juan Perez", order.customerName)
        assertEquals("PE-999", order.vehiclePlate)
        assertEquals(50, order.progress) // 1 of 2 tasks completed
        assertTrue(order.isRisk) // status PENDING and estimatedDate not empty
        assertEquals(2, order.tasks.size)
        assertEquals("Carlos Mecanico", order.tasks[0].mechanicName)
    }

    @Test
    fun `getWorkOrders flags isRisk false when status is not PENDING`() = runTest {
        val ordersDto = listOf(
            WorkOrderDto(
                id = 11,
                workshopId = "WS-01",
                trackingCode = "TRK-011",
                vehicleId = 100,
                customerId = 5,
                mechanicId = 1,
                description = "Revisión",
                status = "IN_PROGRESS",
                price = 100.0,
                estimatedDate = "2026-10-30",
                startDate = "2026-10-24",
                tasksCompleted = false,
                sparePartsChecked = false,
                diagnosisValidated = false,
                cleaningDone = false,
                finalTestDone = false
            )
        )

        whenever(workshopApi.getWorkOrders()).thenReturn(ordersDto)
        whenever(customerApi.getCustomers()).thenReturn(emptyList())
        whenever(fleetApi.vehicles()).thenReturn(emptyList())
        whenever(staffApi.getMechanics()).thenReturn(emptyList())
        whenever(taskApi.getTasks()).thenReturn(emptyList())

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        val order = result.getOrNull()!![0]
        assertFalse(order.isRisk)
        assertEquals("---", order.customerName)
        assertEquals("---", order.vehiclePlate)
        assertEquals(0, order.progress)
    }

    @Test
    fun `getFinancialSummary returns mapped domain model`() = runTest {
        val dto = FinancialSummaryDto(
            projectedRevenue = 5000.0,
            realizedRevenue = 3000.0,
            pendingRevenue = 2000.0,
            operatingCost = 1500.0,
            grossProfit = 1500.0,
            marginPercentage = 50.0,
            profitableOrders = 10,
            lossOrders = 1,
            averageTicket = 300.0
        )
        whenever(workshopApi.getFinancialSummary()).thenReturn(dto)

        val result = repository.getFinancialSummary()

        assertTrue(result.isSuccess)
        val summary = result.getOrNull()!!
        assertEquals(5000.0, summary.projectedRevenue, 0.001)
        assertEquals(50.0, summary.marginPercentage, 0.001)
    }

    @Test
    fun `createTask delegates to taskApi with mapped CreateTaskRequest`() = runTest {
        val input = CreateTaskInput(
            orderId = 10,
            description = "Cambio de bujías",
            priority = "MEDIUM",
            estimatedMinutes = 40,
            laborPrice = 60.0,
            mechanicId = 2
        )

        val result = repository.createTask(input)

        assertTrue(result.isSuccess)
        verify(taskApi).createTask(
            CreateTaskRequest(
                workOrderId = 10,
                mechanicId = 2,
                description = "Cambio de bujías",
                priority = "MEDIUM",
                estimatedTime = 40,
                laborPrice = 60.0
            )
        )
    }

    @Test
    fun `updateTask delegates to taskApi with mapped UpdateTaskRequest`() = runTest {
        val input = UpdateTaskInput(
            taskId = 25,
            description = "Ajuste de frenos",
            priority = "HIGH",
            estimatedMinutes = 50,
            laborPrice = 75.0,
            mechanicId = 4,
            status = TaskStatus.IN_PROGRESS
        )

        val result = repository.updateTask(input)

        assertTrue(result.isSuccess)
        verify(taskApi).updateTask(
            id = 25,
            request = UpdateTaskRequest(
                description = "Ajuste de frenos",
                status = "IN_PROGRESS",
                priority = "HIGH",
                estimatedTime = 50,
                laborPrice = 75.0,
                mechanicId = 4
            )
        )
    }

    @Test
    fun `cancelTask delegates to taskApi deleteTask`() = runTest {
        val taskId = 25

        val result = repository.cancelTask(taskId)

        assertTrue(result.isSuccess)
        verify(taskApi).deleteTask(taskId)
    }

    @Test
    fun `updateTaskStatus delegates to taskApi patchTask with api status value`() = runTest {
        val taskId = 25

        val result = repository.updateTaskStatus(taskId, TaskStatus.COMPLETED)

        assertTrue(result.isSuccess)
        verify(taskApi).patchTask(id = 25, request = PatchTaskRequest(status = "COMPLETED"))
    }
}
