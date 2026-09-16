package com.torquelab.autoservice.workshop.data.repository

import com.torquelab.autoservice.workshop.data.remote.TaskApi
import com.torquelab.autoservice.workshop.data.remote.WorkshopApi
import com.torquelab.autoservice.workshop.data.remote.dto.CreateTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.PatchTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.TaskDto
import com.torquelab.autoservice.workshop.data.remote.dto.UpdateTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.WorkOrderDto
import com.torquelab.autoservice.workshop.domain.model.CreateTaskInput
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.UpdateTaskInput
import com.torquelab.autoservice.workshop.domain.model.WorkshopTask
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder
import com.torquelab.autoservice.workshop.domain.repository.WorkshopRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkshopRepositoryImpl @Inject constructor(
    private val workshopApi: WorkshopApi,
    private val taskApi: TaskApi
) : WorkshopRepository {

    override suspend fun getWorkOrders(): Result<List<WorkshopWorkOrder>> = runCatching {
        workshopApi.getWorkOrders().map { order ->
            val tasks = taskApi.getTasks(workOrderId = order.id).map { task ->
                task.toDomain()
            }
            order.toDomain(tasks)
        }
    }

    override suspend fun createTask(input: CreateTaskInput): Result<Unit> = runCatching {
        taskApi.createTask(
            CreateTaskRequest(
                workOrderId = input.orderId,
                mechanicId = input.mechanicId,
                description = input.description,
                priority = input.priority,
                estimatedTime = input.estimatedMinutes,
                laborPrice = input.laborPrice
            )
        )
        Unit
    }

    override suspend fun updateTask(input: UpdateTaskInput): Result<Unit> = runCatching {
        taskApi.updateTask(
            id = input.taskId,
            request = UpdateTaskRequest(
                description = input.description,
                status = input.status.toApiValue(),
                priority = input.priority,
                estimatedTime = input.estimatedMinutes,
                laborPrice = input.laborPrice,
                mechanicId = input.mechanicId
            )
        )
        Unit
    }

    override suspend fun cancelTask(taskId: Int): Result<Unit> = runCatching {
        taskApi.deleteTask(taskId)
        Unit
    }

    override suspend fun updateTaskStatus(
        taskId: Int,
        status: TaskStatus
    ): Result<Unit> = runCatching {
        taskApi.patchTask(
            id = taskId,
            request = PatchTaskRequest(status = status.toApiValue())
        )
        Unit
    }

    private fun WorkOrderDto.toDomain(tasks: List<WorkshopTask>) = WorkshopWorkOrder(
        id = id,
        vehicleId = vehicleId,
        customerId = customerId,
        trackingCode = trackingCode,
        description = description,
        status = TaskStatus.fromApi(status),
        progress = calculateProgress(tasks),
        tasks = tasks,
        workshopId = workshopId
    )

    private fun TaskDto.toDomain() = WorkshopTask(
        id = id,
        orderId = workOrderId,
        description = description,
        priority = priority,
        estimatedMinutes = estimatedTime,
        laborPrice = laborPrice,
        status = TaskStatus.fromApi(status),
        mechanicId = mechanicId
    )

    private fun calculateProgress(tasks: List<WorkshopTask>): Int {
        if (tasks.isEmpty()) return 0
        val completed = tasks.count {
            it.status == TaskStatus.COMPLETED || it.status == TaskStatus.DELIVERED
        }
        return (completed * 100) / tasks.size
    }
}
