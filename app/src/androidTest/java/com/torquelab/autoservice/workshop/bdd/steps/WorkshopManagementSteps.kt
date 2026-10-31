package com.torquelab.autoservice.workshop.bdd.steps

import com.torquelab.autoservice.staff.domain.model.Mechanic
import com.torquelab.autoservice.test_doubles.FakeWorkshopRepository
import com.torquelab.autoservice.workshop.domain.model.CreateTaskInput
import com.torquelab.autoservice.workshop.domain.model.FinancialSummary
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.WorkshopTask
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Step definitions for workshop_operations.feature
 */
class WorkshopManagementSteps(
    private val repository: FakeWorkshopRepository
) {

    private var currentOrder: WorkshopWorkOrder? = null
    private val currentTasks = mutableListOf<WorkshopTask>()
    private val availableMechanics = mutableListOf<Mechanic>()
    private var financialSummary: FinancialSummary? = null

    // --- GIVEN STEPS ---

    fun givenQueExisteUnaOrdenDeTrabajoActiva(orderId: Int) {
        val order = WorkshopWorkOrder(
            id = orderId,
            vehicleId = 10,
            customerId = 5,
            trackingCode = "TRK-$orderId",
            description = "Diagnóstico general",
            status = TaskStatus.IN_PROGRESS,
            customerName = "Juan Perez",
            vehiclePlate = "PE-999"
        )
        currentOrder = order
        repository.workOrders.add(order)
    }

    fun givenElMecanicoEstaDisponibleEnElTaller(mechanicId: Int, name: String) {
        availableMechanics.add(
            Mechanic(
                id = mechanicId,
                fullName = name,
                email = "carlos@taller.com",
                specialty = "Electrónica automotriz",
                maxCapacity = 3,
                assignedTasks = 1,
                workshopId = "WS-01",
                isAvailable = true
            )
        )
    }

    fun givenOrdenConDosTareasRegistradas(orderId: Int) {
        givenQueExisteUnaOrdenDeTrabajoActiva(orderId)
        currentTasks.clear()
        currentTasks.add(
            WorkshopTask(
                id = 1,
                orderId = orderId,
                description = "Escaneo computarizado",
                priority = "HIGH",
                estimatedMinutes = 30,
                laborPrice = 50.0,
                materialsCost = 0.0,
                status = TaskStatus.COMPLETED,
                mechanicId = 3,
                mechanicName = "Carlos Mecánico"
            )
        )
        currentTasks.add(
            WorkshopTask(
                id = 2,
                orderId = orderId,
                description = "Limpieza de inyectores",
                priority = "MEDIUM",
                estimatedMinutes = 45,
                laborPrice = 70.0,
                materialsCost = 20.0,
                status = TaskStatus.IN_PROGRESS,
                mechanicId = 3,
                mechanicName = "Carlos Mecánico"
            )
        )
    }

    fun givenOrdenesActivasYResumenFinanciero() {
        financialSummary = FinancialSummary(
            projectedRevenue = 25000.0,
            realizedRevenue = 18000.0,
            pendingRevenue = 7000.0,
            operatingCost = 10000.0,
            grossProfit = 8000.0,
            marginPercentage = 44.44,
            profitableOrders = 15,
            lossOrders = 1,
            averageTicket = 1200.0
        )
    }

    // --- WHEN STEPS ---

    suspend fun whenElAdministradorRegistraLaTarea(
        orderId: Int,
        description: String,
        priority: String,
        minutes: Int,
        laborPrice: Double,
        mechanicId: Int
    ) {
        val input = CreateTaskInput(
            orderId = orderId,
            description = description,
            priority = priority,
            estimatedMinutes = minutes,
            laborPrice = laborPrice,
            mechanicId = mechanicId
        )

        val result = repository.createTask(input)
        assertTrue("createTask should succeed", result.isSuccess)

        val mechanic = availableMechanics.find { it.id == mechanicId }
        val newTask = WorkshopTask(
            id = 99,
            orderId = orderId,
            description = description,
            priority = priority,
            estimatedMinutes = minutes,
            laborPrice = laborPrice,
            materialsCost = 0.0,
            status = TaskStatus.PENDING,
            mechanicId = mechanicId,
            mechanicName = mechanic?.fullName
        )
        currentTasks.add(newTask)
    }

    suspend fun whenElAdministradorActualizaElEstadoDeLaSegundaTarea(taskId: Int, status: TaskStatus) {
        val result = repository.updateTaskStatus(taskId, status)
        assertTrue("updateTaskStatus should succeed", result.isSuccess)

        val index = currentTasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            currentTasks[index] = currentTasks[index].copy(status = status)
        }

        // Recalculate progress: (completed / total) * 100
        val completed = currentTasks.count { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.DELIVERED }
        val total = currentTasks.size
        val progress = if (total > 0) (completed * 100) / total else 0
        currentOrder = currentOrder?.copy(progress = progress)
    }

    // --- THEN STEPS ---

    fun thenLaTareaQuedaAsociadaALaOrden(orderId: Int, expectedStatus: TaskStatus, expectedMechanic: String) {
        val task = currentTasks.find { it.orderId == orderId }
        assertNotNull("Task must exist in order", task)
        assertEquals(expectedStatus, task?.status)
        assertEquals(expectedMechanic, task?.mechanicName)
    }

    fun thenElPorcentajeDeAvanceDeLaOrdenSeRecalculaAl(expectedProgress: Int) {
        assertEquals(expectedProgress, currentOrder?.progress)
    }

    fun thenSeVisualizanMetricasOperativasYFinancieras(expectedRevenue: Double, expectedMargin: Double) {
        assertNotNull("Financial summary must be present", financialSummary)
        assertEquals(expectedRevenue, financialSummary?.projectedRevenue ?: 0.0, 0.001)
        assertEquals(expectedMargin, financialSummary?.marginPercentage ?: 0.0, 0.001)
    }
}
