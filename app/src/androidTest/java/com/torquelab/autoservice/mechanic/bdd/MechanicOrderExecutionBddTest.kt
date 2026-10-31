package com.torquelab.autoservice.mechanic.bdd

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.torquelab.autoservice.mechanic.bdd.steps.MechanicOrderSteps
import com.torquelab.autoservice.test_doubles.FakeMechanicRepository
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Automated BDD Test runner executing scenarios from:
 * assets/features/mechanic/mechanic_order_execution.feature
 */
@RunWith(AndroidJUnit4::class)
class MechanicOrderExecutionBddTest {

    private lateinit var fakeRepository: FakeMechanicRepository
    private lateinit var steps: MechanicOrderSteps

    @Before
    fun setUp() {
        fakeRepository = FakeMechanicRepository()
        steps = MechanicOrderSteps(fakeRepository)
    }

    @Test
    fun scenario_mecanicoIniciaUnaTareaTecnicaAsignada() = runBlocking {
        // Given que el mecánico ha iniciado sesión con su cuenta asignada
        steps.givenQueElMecanicoHaIniciadoSesion(email = "carlos@autoservice.com", mechanicId = 5)

        // And tiene una orden de trabajo activa con id 10 y placa "ABC-123"
        steps.givenTieneUnaOrdenDeTrabajoActiva(orderId = 10, plate = "ABC-123")

        // And la orden tiene una tarea pendiente "Cambio de pastillas de freno" con id 101
        steps.givenLaOrdenTieneUnaTareaPendiente(taskId = 101, description = "Cambio de pastillas de freno")

        // When el mecánico presiona el botón "Iniciar" en la tarea con id 101
        steps.whenElMecanicoPresionaElBotonIniciar(taskId = 101)

        // Then el estado de la tarea cambia a "IN_PROGRESS"
        steps.thenElEstadoDeLaTareaCambiaA(taskId = 101, expectedStatus = TaskStatus.IN_PROGRESS)
    }

    @Test
    fun scenario_mecanicoCompletaUnaTareaAgregandoDiagnosticoTecnico() = runBlocking {
        // Given que la tarea con id 101 se encuentra en estado "IN_PROGRESS"
        steps.givenTieneUnaOrdenDeTrabajoActiva(orderId = 10, plate = "ABC-123")
        steps.givenLaOrdenTieneUnaTareaPendiente(taskId = 101, description = "Cambio de pastillas de freno")
        steps.givenQueLaTareaSeEncuentraEnEstado(taskId = 101, status = TaskStatus.IN_PROGRESS)

        // When el mecánico ingresa el diagnóstico técnico y presiona "Completar Tarea"
        steps.whenElMecanicoIngresaDiagnosticoYCompleta(
            taskId = 101,
            diagnosis = "Discos rectificados y pastillas cerámicas instaladas"
        )

        // Then el estado de la tarea pasa a "COMPLETED"
        steps.thenElEstadoDeLaTareaCambiaA(taskId = 101, expectedStatus = TaskStatus.COMPLETED)

        // And el porcentaje de avance de la orden de trabajo se incrementa al 100 por ciento
        steps.thenElPorcentajeDeAvanceDeLaOrdenEs(expectedPercentage = 100)
    }

    @Test
    fun scenario_mecanicoProponeUnaTareaAdicionalConRepuestosDelInventario() = runBlocking {
        // Given que durante la inspección de la orden 10 se detecta un problema no contemplado
        steps.givenQueElMecanicoHaIniciadoSesion(email = "carlos@autoservice.com", mechanicId = 5)
        steps.givenTieneUnaOrdenDeTrabajoActiva(orderId = 10, plate = "ABC-123")

        // When el mecánico propone la tarea con prioridad "URGENT", 30 minutos y repuesto id 12
        steps.whenElMecanicoProponeUnaTareaAdicional(
            description = "Reemplazo de manguera de freno cuarteada",
            priority = "URGENT",
            minutes = 30,
            diagnosis = "Riesgo inminente de fuga de presión hidráulica",
            partInventoryId = 12,
            quantity = 1
        )

        // Then la solicitud de tarea se registra con estado de revisión "SUBMITTED"
        steps.thenLaSolicitudDeTareaSeRegistraConRevision(expectedStatus = "SUBMITTED")
    }
}
