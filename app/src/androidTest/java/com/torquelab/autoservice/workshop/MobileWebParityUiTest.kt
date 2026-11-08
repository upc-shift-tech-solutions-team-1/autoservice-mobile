package com.torquelab.autoservice.workshop

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.torquelab.autoservice.workshop.domain.model.*
import com.torquelab.autoservice.workshop.presentation.WorkshopScreen
import com.torquelab.autoservice.workshop.presentation.WorkshopUiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real Compose forms. No production API or database writes. */
@RunWith(AndroidJUnit4::class)
class MobileWebParityUiTest {
    @get:Rule val compose = createComposeRule()
    private val task = WorkshopTask(1,1,"Cambio de aceite","MEDIUM",60,80.0,20.0,TaskStatus.COMPLETED,2,adminReviewStatus="APPROVED")
    private val order = WorkshopWorkOrder(1,1,1,"WO-UI","Mantenimiento",TaskStatus.IN_PROGRESS,
        tasks=listOf(task),estimatedDate="2026-11-15",calculatedTotal=100.0)

    private fun mount(onUpdate: (WorkshopWorkOrder, String?) -> Unit = { _, _ -> }) {
        compose.setContent {
            MaterialTheme {
                WorkshopScreen(WorkshopUiState(isLoading=false,workOrders=listOf(order)), {},
                    { _, _, _, _, _, _ -> }, {}, {}, { _, _ -> }, onUpdateOrder=onUpdate)
            }
        }
    }
    private fun openDetail() {
        compose.onNodeWithText("Detalle / calidad").performScrollTo().performClick()
    }

    @Test fun postponingDateRequiresExplanationBeforeSave() {
        var saved: WorkshopWorkOrder? = null
        var reason: String? = null
        mount { update, explanation -> saved = update; reason = explanation }
        openDetail()
        compose.onNodeWithText("Fecha estimada (AAAA-MM-DD)").performTextReplacement("2026-11-16")
        compose.onNodeWithText("Guardar").assertIsNotEnabled()
        compose.onNodeWithText("Motivo de la demora").performScrollTo().performTextInput("Repuesto en traslado")
        compose.onNodeWithText("Guardar").performClick()
        compose.runOnIdle {
            assertEquals("2026-11-16",saved?.estimatedDate)
            assertEquals("Repuesto en traslado",reason)
            assertEquals(100.0,saved!!.calculatedTotal,0.001)
        }
    }

    @Test fun finishingRequiresFiveQualityChecksAndPreservesCompletedTasks() {
        var saved: WorkshopWorkOrder? = null
        mount { update, _ -> saved = update }
        openDetail()
        compose.onNodeWithText("Finalizar orden").performScrollTo().assertIsNotEnabled()
        repeat(5) { index -> compose.onAllNodes(isToggleable())[index].performScrollTo().performClick() }
        compose.onNodeWithText("Finalizar orden").performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals(TaskStatus.FINISHED,saved?.status)
            assertTrue(saved!!.checklist.complete)
            assertEquals(listOf(task),saved!!.tasks)
        }
    }

    @Test fun emptyWorkshopStillAllowsCreatingFirstOrder() {
        compose.setContent {
            MaterialTheme { WorkshopScreen(WorkshopUiState(isLoading=false), {}, { _, _, _, _, _, _ -> }, {}, {}, { _, _ -> }) }
        }
        compose.onNodeWithText("Nueva orden").assertIsEnabled().performClick()
        compose.onNodeWithText("Guardar").assertIsNotEnabled()
        compose.onNodeWithText("Registre primero clientes, vehículos y mecánicos desde sus módulos.").performScrollTo().assertIsDisplayed()
    }

    @Test fun completionPreservesCurrentDiagnosisInTaskReport() {
        var saved: com.torquelab.autoservice.mechanic.domain.model.TaskReport? = null
        var completed = false
        val mechanicTask = com.torquelab.autoservice.mechanic.domain.model.MechanicTask(
            1,1,"Revisar motor",TaskStatus.IN_PROGRESS,"MEDIUM",60,80.0,0.0,"","APPROVED",emptyList(),mechanicId=2)
        val mechanicOrder = com.torquelab.autoservice.mechanic.domain.model.MechanicOrder(
            1,"WO-UI",1,1,2,"Mantenimiento","IN_PROGRESS",80.0,"Toyota Corolla","ABC-123")
        compose.setContent {
            MaterialTheme {
                com.torquelab.autoservice.mechanic.presentation.order_execution.MechanicOrderExecutionScreen(
                    com.torquelab.autoservice.mechanic.presentation.order_execution.MechanicOrderExecutionState(
                        order=mechanicOrder,tasks=listOf(mechanicTask),diagnosisText="Aceite degradado"),
                    mechanicId=2,onBack={},onRetry={},onUpdateDiagnosis={},onStartTask={},onCompleteTask={},onProposeTask={},
                    onSaveReport={ _, report, complete -> saved = report; completed = complete })
            }
        }
        compose.onNodeWithText("Completar").performScrollTo().performClick()
        compose.onNodeWithText("Guardar").performClick()
        compose.runOnIdle {
            assertEquals("Aceite degradado",saved?.technicalDiagnosis)
            assertTrue(completed)
        }
    }
}
