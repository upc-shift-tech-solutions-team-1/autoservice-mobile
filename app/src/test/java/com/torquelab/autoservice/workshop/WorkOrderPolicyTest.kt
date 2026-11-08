package com.torquelab.autoservice.workshop

import com.torquelab.autoservice.workshop.domain.model.*
import com.torquelab.autoservice.mechanic.domain.model.TaskReport
import com.torquelab.autoservice.shared.domain.isWebEvidence
import org.junit.Test
import org.junit.Assert.*

class WorkOrderPolicyTest {
    private val approved = WorkshopTask(1,1,"Reparación","MEDIUM",60,100.0,20.0,TaskStatus.COMPLETED,1,adminReviewStatus="APPROVED")
    private val order = WorkshopWorkOrder(1,1,1,"WO-1","Reparación",TaskStatus.IN_PROGRESS,tasks=listOf(approved))
    private val complete = QualityChecklist(true,true,true,true,true)
    @Test fun `postponing date requires reason while same date and earlier date do not`() {
        assertNotNull(validateEstimatedDate("2026-11-10","2026-11-11","  "))
        assertNull(validateEstimatedDate("2026-11-10","2026-11-11","Repuesto en traslado"))
        assertNull(validateEstimatedDate("2026-11-10","2026-11-09",null))
        assertNull(validateEstimatedDate("2026-11-10","2026-11-10",null))
        assertNotNull(validateEstimatedDate(null,"2026-02-30",null))
    }
    @Test fun `finishing needs all checks completed approved tasks and an open order`() {
        assertFalse(order.canFinish())
        assertTrue(order.canFinish(complete))
        assertFalse(order.copy(tasks=emptyList()).canFinish(complete))
        assertFalse(order.copy(tasks=listOf(approved.copy(adminReviewStatus="SUBMITTED"))).canFinish(complete))
        assertFalse(order.copy(tasks=listOf(approved.copy(status=TaskStatus.IN_PROGRESS))).canFinish(complete))
        assertFalse(order.copy(status=TaskStatus.FINISHED).canFinish(complete))
        assertFalse(order.copy(status=TaskStatus.DELIVERED).canFinish(complete))
    }
    @Test fun `finished backend order is recognized without treating it as unknown`() {
        assertEquals(TaskStatus.FINISHED,TaskStatus.fromApi("FINISHED"))
        assertEquals("FINISHED",TaskStatus.FINISHED.toApiValue())
    }
    @Test fun `report allows partial progress and validates optional evidence links`() {
        assertNull(TaskReport("","Explicación","","").validationError())
        assertNull(TaskReport("Diagnóstico","","","").validationError())
        assertNotNull(TaskReport("Diagnóstico","Explicación","","javascript:alert(1)").validationError())
        assertFalse(isWebEvidence("file:///data/private"))
        assertFalse(isWebEvidence("https://user:pass@example.com/test"))
        assertNull(TaskReport("Diagnóstico","Explicación","Privada","https://example.com/evidencia").validationError())
    }
}
