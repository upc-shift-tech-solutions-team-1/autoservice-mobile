package com.torquelab.autoservice.workshop.domain.repository

import com.torquelab.autoservice.workshop.domain.model.CreateTaskInput
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.UpdateTaskInput
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder

interface WorkshopRepository {

    suspend fun getWorkOrders(): Result<List<WorkshopWorkOrder>>

    suspend fun createTask(input: CreateTaskInput): Result<Unit>

    suspend fun updateTask(input: UpdateTaskInput): Result<Unit>

    suspend fun cancelTask(taskId: Int): Result<Unit>

    suspend fun updateTaskStatus(taskId: Int, status: TaskStatus): Result<Unit>

}
