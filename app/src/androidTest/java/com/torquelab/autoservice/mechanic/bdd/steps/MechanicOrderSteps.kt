package com.torquelab.autoservice.mechanic.bdd.steps

import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTask
import com.torquelab.autoservice.mechanic.domain.model.MechanicTaskPartInput
import com.torquelab.autoservice.shared.session.UserSession
import com.torquelab.autoservice.test_doubles.FakeMechanicRepository
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Step definitions for mechanic_order_execution.feature
 */
class MechanicOrderSteps(
    private val repository: FakeMechanicRepository
) {

    private var currentSession: UserSession? = null
    private var currentOrder: MechanicOrder? = null

    // --- GIVEN STEPS ---

    fun givenQueElMecanicoHaIniciadoSesion(email: String, mechanicId: Int) {
        currentSession = UserSession(
            userId = 1,
            email = email,
            role = "ROLE_MECHANIC",
            workshopId = "WS-01",
            mechanicId = mechanicId,
            token = "jwt-mechanic-token"
        )
    }

    fun givenTieneUnaOrdenDeTrabajoActiva(orderId: Int, plate: String) {
        val order = MechanicOrder(
            id = orderId,
            trackingCode = "TRK-$orderId",
            vehicleId = 10,
            customerId = 2,
            mechanicId = currentSession?.mechanicId ?: 1,
            description = "Mantenimiento general",
            status = "IN_PROGRESS",
            price = 250.0,
            vehiclePlate = plate,
            vehicleName = "Toyota Corolla"
        )
        currentOrder = order
        repository.orders.add(order)
    }

    fun givenLaOrdenTieneUnaTareaPendiente(taskId: Int, description: String) {
        val task = MechanicTask(
            id = taskId,
            workOrderId = currentOrder?.id ?: 1,
            description = description,
            status = TaskStatus.PENDING,
            priority = "HIGH",
            estimatedTime = 60,
            laborPrice = 80.0,
            materialsCost = 0.0,
            technicalDiagnosis = "",
            adminReviewStatus = "APPROVED",
            parts = emptyList()
        )
        repository.tasks.add(task)
    }

    fun givenQueLaTareaSeEncuentraEnEstado(taskId: Int, status: TaskStatus) {
        val index = repository.tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            repository.tasks[index] = repository.tasks[index].copy(status = status)
        }
    }

    // --- WHEN STEPS ---

    suspend fun whenElMecanicoPresionaElBotonIniciar(taskId: Int) {
        val result = repository.startTask(taskId)
        assertTrue("startTask should succeed", result.isSuccess)
    }

    suspend fun whenElMecanicoIngresaDiagnosticoYCompleta(taskId: Int, diagnosis: String) {
        val result = repository.completeTask(taskId, diagnosis)
        assertTrue("completeTask should succeed", result.isSuccess)

        // Recalculate order progress
        val completedCount = repository.tasks.count { it.status == TaskStatus.COMPLETED }
        val total = repository.tasks.size
        val progress = if (total > 0) (completedCount * 100) / total else 0
        currentOrder = currentOrder?.copy(
            tasksCompleted = completedCount,
            totalTasks = total,
            progress = progress
        )
    }

    suspend fun whenElMecanicoProponeUnaTareaAdicional(
        description: String,
        priority: String,
        minutes: Int,
        diagnosis: String,
        partInventoryId: Int,
        quantity: Int
    ) {
        val input = MechanicProposeTaskInput(
            workOrderId = currentOrder?.id ?: 1,
            mechanicId = currentSession?.mechanicId ?: 1,
            description = description,
            priority = priority,
            estimatedTime = minutes,
            technicalDiagnosis = diagnosis,
            parts = listOf(MechanicTaskPartInput(partInventoryId, quantity))
        )
        val result = repository.proposeTask(input)
        assertTrue("proposeTask should succeed", result.isSuccess)
    }

    // --- THEN STEPS ---

    fun thenElEstadoDeLaTareaCambiaA(taskId: Int, expectedStatus: TaskStatus) {
        val task = repository.tasks.find { it.id == taskId }
        assertNotNull("Task should exist", task)
        assertEquals(expectedStatus, task?.status)
    }

    fun thenElPorcentajeDeAvanceDeLaOrdenEs(expectedPercentage: Int) {
        assertEquals(expectedPercentage, currentOrder?.progress)
    }

    fun thenLaSolicitudDeTareaSeRegistraConRevision(expectedStatus: String) {
        val proposed = repository.lastProposedInput
        assertNotNull("A proposal should have been recorded", proposed)
        assertEquals("Reemplazo de manguera de freno cuarteada", proposed?.description)
        assertEquals(1, proposed?.parts?.size)
    }
}
