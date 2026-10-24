package com.torquelab.autoservice.mechanic.data.repository

import com.torquelab.autoservice.fleet.data.FleetApi
import com.torquelab.autoservice.fleet.domain.Vehicle
import com.torquelab.autoservice.inventory.data.InventoryApi
import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTaskPartInput
import com.torquelab.autoservice.workshop.data.remote.TaskApi
import com.torquelab.autoservice.workshop.data.remote.WorkshopApi
import com.torquelab.autoservice.workshop.data.remote.dto.CreateTaskPartRequest
import com.torquelab.autoservice.workshop.data.remote.dto.CreateTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.PatchTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.TaskDto
import com.torquelab.autoservice.workshop.data.remote.dto.WorkOrderDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class MechanicRepositoryImplUnitTest {

    private lateinit var workshopApi: WorkshopApi
    private lateinit var taskApi: TaskApi
    private lateinit var fleetApi: FleetApi
    private lateinit var inventoryApi: InventoryApi
    private lateinit var repository: MechanicRepositoryImpl

    @Before
    fun setUp() {
        workshopApi = mock(WorkshopApi::class.java)
        taskApi = mock(TaskApi::class.java)
        fleetApi = mock(FleetApi::class.java)
        inventoryApi = mock(InventoryApi::class.java)
        repository = MechanicRepositoryImpl(workshopApi, taskApi, fleetApi, inventoryApi)
    }

    @Test
    fun `getOrdersForMechanic filters by mechanicId and calculates progress and costs correctly`() = runTest {
        val mechanicId = 5
        val ordersDto = listOf(
            WorkOrderDto(
                id = 1,
                workshopId = "WS-01",
                trackingCode = "TRK-001",
                vehicleId = 10,
                customerId = 2,
                mechanicId = mechanicId,
                description = "Mantenimiento preventivo 20k",
                status = "IN_PROGRESS",
                price = 250.0,
                estimatedDate = "2026-10-25",
                startDate = "2026-10-24",
                tasksCompleted = false,
                sparePartsChecked = true,
                diagnosisValidated = true,
                cleaningDone = false,
                finalTestDone = false
            ),
            WorkOrderDto(
                id = 2,
                workshopId = "WS-01",
                trackingCode = "TRK-002",
                vehicleId = 20,
                customerId = 3,
                mechanicId = 999, // Another mechanic
                description = "Alineación",
                status = "PENDING",
                price = 80.0,
                estimatedDate = "",
                startDate = "2026-10-24",
                tasksCompleted = false,
                sparePartsChecked = false,
                diagnosisValidated = false,
                cleaningDone = false,
                finalTestDone = false
            )
        )

        val vehicles = listOf(
            Vehicle(id = 10, plate = "ABC-123", brand = "Toyota", model = "Corolla")
        )

        val tasks = listOf(
            TaskDto(
                id = 101,
                workOrderId = 1,
                mechanicId = mechanicId,
                description = "Cambio de aceite",
                status = "COMPLETED",
                priority = "MEDIUM",
                estimatedTime = 30,
                laborPrice = 50.0,
                materialsCost = 70.0
            ),
            TaskDto(
                id = 102,
                workOrderId = 1,
                mechanicId = mechanicId,
                description = "Filtro de aire",
                status = "IN_PROGRESS",
                priority = "LOW",
                estimatedTime = 20,
                laborPrice = 30.0,
                materialsCost = 25.0
            )
        )

        whenever(workshopApi.getWorkOrders()).thenReturn(ordersDto)
        whenever(fleetApi.vehicles()).thenReturn(vehicles)
        whenever(taskApi.getTasks()).thenReturn(tasks)

        val result = repository.getOrdersForMechanic(mechanicId)

        assertTrue(result.isSuccess)
        val orders = result.getOrNull()!!
        assertEquals(1, orders.size)

        val order = orders[0]
        assertEquals(1, order.id)
        assertEquals("TRK-001", order.trackingCode)
        assertEquals("Toyota Corolla", order.vehicleName)
        assertEquals("ABC-123", order.vehiclePlate)
        assertEquals(1, order.tasksCompleted)
        assertEquals(2, order.totalTasks)
        assertEquals(50, order.progress) // 1 of 2 = 50%
        assertEquals(80.0, order.totalLaborCost, 0.001) // 50 + 30
        assertEquals(95.0, order.totalMaterialsCost, 0.001) // 70 + 25
    }

    @Test
    fun `getOrdersForMechanic handles order with 0 tasks safely`() = runTest {
        val mechanicId = 3
        val ordersDto = listOf(
            WorkOrderDto(
                id = 5,
                workshopId = "WS-01",
                trackingCode = "TRK-005",
                vehicleId = 99,
                customerId = 1,
                mechanicId = mechanicId,
                description = "Revisión rápida",
                status = "PENDING",
                price = 100.0,
                estimatedDate = "",
                startDate = "2026-10-24",
                tasksCompleted = false,
                sparePartsChecked = false,
                diagnosisValidated = false,
                cleaningDone = false,
                finalTestDone = false
            )
        )

        whenever(workshopApi.getWorkOrders()).thenReturn(ordersDto)
        whenever(fleetApi.vehicles()).thenReturn(emptyList()) // Vehicle not found fallback
        whenever(taskApi.getTasks()).thenReturn(emptyList())

        val result = repository.getOrdersForMechanic(mechanicId)

        assertTrue(result.isSuccess)
        val order = result.getOrNull()!![0]
        assertEquals("Vehículo", order.vehicleName)
        assertEquals("Sin placa", order.vehiclePlate)
        assertEquals(0, order.totalTasks)
        assertEquals(0, order.tasksCompleted)
        assertEquals(0, order.progress)
    }

    @Test
    fun `getOrderById returns mapped order with progress`() = runTest {
        val orderId = 1
        val orderDto = WorkOrderDto(
            id = orderId,
            workshopId = "WS-01",
            trackingCode = "TRK-001",
            vehicleId = 10,
            customerId = 2,
            mechanicId = 5,
            description = "Frenos",
            status = "IN_PROGRESS",
            price = 180.0,
            estimatedDate = "2026-10-25",
            startDate = "2026-10-24",
            tasksCompleted = false,
            sparePartsChecked = true,
            diagnosisValidated = true,
            cleaningDone = false,
            finalTestDone = false
        )

        val vehicle = Vehicle(id = 10, plate = "XYZ-789", brand = "Nissan", model = "Sentra")
        val tasks = listOf(
            TaskDto(
                id = 101,
                workOrderId = orderId,
                mechanicId = 5,
                description = "Pastillas",
                status = "COMPLETED",
                priority = "HIGH",
                estimatedTime = 60,
                laborPrice = 80.0,
                materialsCost = 100.0
            )
        )

        whenever(workshopApi.getWorkOrder(orderId)).thenReturn(orderDto)
        whenever(fleetApi.vehicles()).thenReturn(listOf(vehicle))
        whenever(taskApi.getTasks(workOrderId = orderId)).thenReturn(tasks)

        val result = repository.getOrderById(orderId)

        assertTrue(result.isSuccess)
        val order = result.getOrNull()!!
        assertEquals(orderId, order.id)
        assertEquals("Nissan Sentra", order.vehicleName)
        assertEquals("XYZ-789", order.vehiclePlate)
        assertEquals(1, order.tasksCompleted)
        assertEquals(100, order.progress)
    }

    @Test
    fun `proposeTask sends correct CreateTaskRequest payload with SUBMITTED status`() = runTest {
        val input = MechanicProposeTaskInput(
            workOrderId = 10,
            mechanicId = 5,
            description = "Cambio de líquido de frenos no previsto",
            priority = "HIGH",
            estimatedTime = 45,
            technicalDiagnosis = "Líquido contaminado con humedad superior a 3%",
            parts = listOf(
                MechanicTaskPartInput(inventoryItemId = 12, quantity = 2)
            )
        )

        val expectedRequest = CreateTaskRequest(
            workOrderId = 10,
            mechanicId = 5,
            description = "Cambio de líquido de frenos no previsto",
            priority = "HIGH",
            estimatedTime = 45,
            laborPrice = 0.0,
            technicalDiagnosis = "Líquido contaminado con humedad superior a 3%",
            adminReviewStatus = "SUBMITTED",
            parts = listOf(CreateTaskPartRequest(inventoryItemId = 12, quantity = 2))
        )

        val result = repository.proposeTask(input)

        assertTrue(result.isSuccess)
        verify(taskApi).createTask(expectedRequest)
    }

    @Test
    fun `startTask calls patchTask with IN_PROGRESS status`() = runTest {
        val taskId = 42

        val result = repository.startTask(taskId)

        assertTrue(result.isSuccess)
        verify(taskApi).patchTask(taskId, PatchTaskRequest(status = "IN_PROGRESS"))
    }

    @Test
    fun `completeTask calls patchTask with COMPLETED status and diagnosis`() = runTest {
        val taskId = 42
        val diagnosis = "Calibración final completada según manual"

        val result = repository.completeTask(taskId, diagnosis)

        assertTrue(result.isSuccess)
        verify(taskApi).patchTask(taskId, PatchTaskRequest(status = "COMPLETED", technicalDiagnosis = diagnosis))
    }

    @Test
    fun `api exceptions return Result failure`() = runTest {
        whenever(workshopApi.getWorkOrders()).thenThrow(RuntimeException("Network timeout"))

        val result = repository.getOrdersForMechanic(1)

        assertTrue(result.isFailure)
        assertEquals("Network timeout", result.exceptionOrNull()?.message)
    }
}
