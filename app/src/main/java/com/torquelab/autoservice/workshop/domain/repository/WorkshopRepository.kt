package com.torquelab.autoservice.workshop.domain.repository

import com.torquelab.autoservice.workshop.domain.model.CreateTaskInput
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.UpdateTaskInput
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder

interface WorkshopRepository {
    suspend fun getOptions(): Result<com.torquelab.autoservice.workshop.domain.model.WorkshopOptions>
    suspend fun createWorkOrder(input: com.torquelab.autoservice.workshop.domain.model.CreateWorkOrderInput): Result<Unit>
    suspend fun updateWorkOrder(order: WorkshopWorkOrder, delayReason: String? = null): Result<Unit>

    suspend fun getWorkOrders(): Result<List<WorkshopWorkOrder>>

    suspend fun getFinancialSummary(): Result<com.torquelab.autoservice.workshop.domain.model.FinancialSummary>

    suspend fun createTask(input: CreateTaskInput): Result<Unit>

    suspend fun updateTask(input: UpdateTaskInput): Result<Unit>

    suspend fun cancelTask(taskId: Int): Result<Unit>

    suspend fun updateTaskStatus(taskId: Int, status: TaskStatus): Result<Unit>

}
