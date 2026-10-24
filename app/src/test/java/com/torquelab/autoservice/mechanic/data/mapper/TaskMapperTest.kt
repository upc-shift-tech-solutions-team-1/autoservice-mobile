package com.torquelab.autoservice.mechanic.data.mapper

import com.torquelab.autoservice.workshop.data.remote.dto.TaskDto
import com.torquelab.autoservice.workshop.data.remote.dto.TaskPartDto
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskMapperTest {

    @Test
    fun `toMechanicTask maps all dto fields correctly to domain model`() {
        val dto = TaskDto(
            id = 101,
            workOrderId = 12,
            mechanicId = 5,
            description = "Cambio de pastillas de freno",
            status = "IN_PROGRESS",
            priority = "HIGH",
            estimatedTime = 90,
            laborPrice = 120.0,
            materialsCost = 45.0,
            technicalDiagnosis = "Desgaste severo en pastillas delanteras",
            adminReviewStatus = "APPROVED",
            parts = listOf(
                TaskPartDto(
                    id = 1,
                    inventoryItemId = 10,
                    name = "Pastillas de Freno Bosch",
                    quantity = 2,
                    unitPrice = 50.0,
                    purchasePrice = 30.0,
                    brand = "Bosch",
                    qualityTier = "PREMIUM"
                )
            )
        )

        val task = dto.toMechanicTask()

        assertEquals(101, task.id)
        assertEquals(12, task.workOrderId)
        assertEquals("Cambio de pastillas de freno", task.description)
        assertEquals(TaskStatus.IN_PROGRESS, task.status)
        assertEquals("HIGH", task.priority)
        assertEquals(90, task.estimatedTime)
        assertEquals(120.0, task.laborPrice, 0.001)
        assertEquals(45.0, task.materialsCost, 0.001)
        assertEquals("Desgaste severo en pastillas delanteras", task.technicalDiagnosis)
        assertEquals("APPROVED", task.adminReviewStatus)
        assertEquals(1, task.parts.size)

        val part = task.parts[0]
        assertEquals(10, part.inventoryItemId)
        assertEquals("Pastillas de Freno Bosch", part.name)
        assertEquals(2, part.quantity)
        assertEquals(50.0, part.unitPrice, 0.001)
        assertEquals(30.0, part.purchasePrice, 0.001)
        assertEquals("Bosch", part.brand)
        assertEquals("PREMIUM", part.qualityTier)
    }

    @Test
    fun `toMechanicTask handles nullable fields with safe defaults`() {
        val dto = TaskDto(
            id = 102,
            workOrderId = 15,
            mechanicId = null,
            description = "Diagnóstico general",
            status = "UNKNOWN_STATUS",
            priority = "MEDIUM",
            estimatedTime = 30,
            laborPrice = 50.0,
            materialsCost = null,
            technicalDiagnosis = null,
            adminReviewStatus = null,
            parts = null
        )

        val task = dto.toMechanicTask()

        assertEquals(102, task.id)
        assertEquals(TaskStatus.UNKNOWN, task.status)
        assertEquals(0.0, task.materialsCost, 0.001)
        assertEquals("", task.technicalDiagnosis)
        assertEquals("", task.adminReviewStatus)
        assertTrue(task.parts.isEmpty())
    }

    @Test
    fun `toMechanicTask handles parts with null brand and qualityTier`() {
        val dto = TaskDto(
            id = 103,
            workOrderId = 18,
            mechanicId = 1,
            description = "Reemplazo de filtro",
            status = "COMPLETED",
            priority = "LOW",
            estimatedTime = 20,
            laborPrice = 30.0,
            parts = listOf(
                TaskPartDto(
                    id = 2,
                    inventoryItemId = 25,
                    name = "Filtro de aire genérico",
                    quantity = 1,
                    unitPrice = 25.0,
                    purchasePrice = 15.0,
                    brand = null,
                    qualityTier = null
                )
            )
        )

        val task = dto.toMechanicTask()

        assertEquals(1, task.parts.size)
        assertEquals("", task.parts[0].brand)
        assertEquals("", task.parts[0].qualityTier)
    }
}
