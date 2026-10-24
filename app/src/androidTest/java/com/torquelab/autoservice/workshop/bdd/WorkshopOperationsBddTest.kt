package com.torquelab.autoservice.workshop.bdd

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.torquelab.autoservice.test_doubles.FakeWorkshopRepository
import com.torquelab.autoservice.workshop.bdd.steps.WorkshopManagementSteps
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Automated BDD Test runner executing scenarios from:
 * assets/features/workshop/workshop_operations.feature
 */
@RunWith(AndroidJUnit4::class)
class WorkshopOperationsBddTest {

    private lateinit var fakeRepository: FakeWorkshopRepository
    private lateinit var steps: WorkshopManagementSteps

    @Before
    fun setUp() {
        fakeRepository = FakeWorkshopRepository()
        steps = WorkshopManagementSteps(fakeRepository)
    }

    @Test
    fun scenario_administradorRegistraNuevaTareaTecnicaAsignandoMecanico() = runBlocking {
        // Given que existe una orden de trabajo activa con id 15
        steps.givenQueExisteUnaOrdenDeTrabajoActiva(orderId = 15)

        // And el mecánico "Carlos Mecánico" con id 3 está disponible en el taller
        steps.givenElMecanicoEstaDisponibleEnElTaller(mechanicId = 3, name = "Carlos Mecánico")

        // When el administrador registra la tarea con prioridad "HIGH", 45 minutos y mano de obra 80.0
        steps.whenElAdministradorRegistraLaTarea(
            orderId = 15,
            description = "Diagnóstico electrónico con scanner OBD2",
            priority = "HIGH",
            minutes = 45,
            laborPrice = 80.0,
            mechanicId = 3
        )

        // Then la tarea queda asociada a la orden de trabajo 15 con estado PENDING y mecánico asignado
        steps.thenLaTareaQuedaAsociadaALaOrden(
            orderId = 15,
            expectedStatus = TaskStatus.PENDING,
            expectedMechanic = "Carlos Mecánico"
        )
    }

    @Test
    fun scenario_administradorActualizaEstadoDeTareaYSeRecalculaProgreso() = runBlocking {
        // Given que una orden de trabajo con id 15 tiene 2 tareas (una COMPLETED y una IN_PROGRESS)
        steps.givenOrdenConDosTareasRegistradas(orderId = 15)

        // When el administrador actualiza el estado de la segunda tarea a "COMPLETED"
        steps.whenElAdministradorActualizaElEstadoDeLaSegundaTarea(taskId = 2, status = TaskStatus.COMPLETED)

        // Then el porcentaje de avance de la orden de trabajo se recalcula al 100 por ciento
        steps.thenElPorcentajeDeAvanceDeLaOrdenSeRecalculaAl(expectedProgress = 100)
    }

    @Test
    fun scenario_administradorConsultaMetricasEnElDashboardFinanciero() {
        // Given que existen órdenes de trabajo activas y un resumen financiero consolidado
        steps.givenOrdenesActivasYResumenFinanciero()

        // Then se visualizan los indicadores clave de ingresos y margen
        steps.thenSeVisualizanMetricasOperativasYFinancieras(
            expectedRevenue = 25000.0,
            expectedMargin = 44.44
        )
    }
}
