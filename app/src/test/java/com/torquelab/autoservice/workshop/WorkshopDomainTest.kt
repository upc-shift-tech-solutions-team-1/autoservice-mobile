package com.torquelab.autoservice.workshop

import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkshopDomainTest {

    @Test
    fun `maps backend task statuses`() {
        assertEquals(TaskStatus.PENDING, TaskStatus.fromApi("PENDING"))
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.fromApi("IN_PROGRESS"))
        assertEquals(TaskStatus.COMPLETED, TaskStatus.fromApi("COMPLETED"))
        assertEquals(TaskStatus.DELIVERED, TaskStatus.fromApi("DELIVERED"))
    }

    @Test
    fun `unknown backend status is safe for the UI`() {
        assertEquals(TaskStatus.UNKNOWN, TaskStatus.fromApi("other-status"))
    }

    @Test
    fun `status generates backend value`() {
        assertEquals("IN_PROGRESS", TaskStatus.IN_PROGRESS.toApiValue())
    }
}
