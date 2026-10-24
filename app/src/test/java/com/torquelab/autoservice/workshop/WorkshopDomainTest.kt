package com.torquelab.autoservice.workshop

import com.torquelab.autoservice.workshop.data.remote.dto.FinancialSummaryDto
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals(TaskStatus.UNKNOWN, TaskStatus.fromApi(null))
    }

    @Test
    fun `status generates backend value`() {
        assertEquals("IN_PROGRESS", TaskStatus.IN_PROGRESS.toApiValue())
        assertEquals("COMPLETED", TaskStatus.COMPLETED.toApiValue())
        assertEquals("PENDING", TaskStatus.PENDING.toApiValue())
    }

    @Test
    fun `financial summary dto maps null fields to safe zero defaults`() {
        val dto = FinancialSummaryDto(
            projectedRevenue = null,
            realizedRevenue = null,
            pendingRevenue = null,
            operatingCost = null,
            grossProfit = null,
            marginPercentage = null,
            profitableOrders = null,
            lossOrders = null,
            averageTicket = null
        )

        val domain = dto.toDomain()

        assertNotNull(domain)
        assertEquals(0.0, domain.projectedRevenue, 0.001)
        assertEquals(0.0, domain.realizedRevenue, 0.001)
        assertEquals(0.0, domain.pendingRevenue, 0.001)
        assertEquals(0.0, domain.operatingCost, 0.001)
        assertEquals(0.0, domain.grossProfit, 0.001)
        assertEquals(0.0, domain.marginPercentage, 0.001)
        assertEquals(0, domain.profitableOrders)
        assertEquals(0, domain.lossOrders)
        assertEquals(0.0, domain.averageTicket, 0.001)
    }

    @Test
    fun `financial summary dto maps valid numbers accurately`() {
        val dto = FinancialSummaryDto(
            projectedRevenue = 15000.50,
            realizedRevenue = 11200.00,
            pendingRevenue = 3800.50,
            operatingCost = 6500.00,
            grossProfit = 4700.00,
            marginPercentage = 41.96,
            profitableOrders = 18,
            lossOrders = 2,
            averageTicket = 750.00
        )

        val domain = dto.toDomain()

        assertEquals(15000.50, domain.projectedRevenue, 0.001)
        assertEquals(11200.00, domain.realizedRevenue, 0.001)
        assertEquals(4700.00, domain.grossProfit, 0.001)
        assertEquals(41.96, domain.marginPercentage, 0.001)
        assertEquals(18, domain.profitableOrders)
        assertEquals(2, domain.lossOrders)
    }
}
