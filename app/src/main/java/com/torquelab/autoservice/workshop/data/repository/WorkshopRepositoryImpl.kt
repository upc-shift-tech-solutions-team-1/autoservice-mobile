package com.torquelab.autoservice.workshop.data.repository

import com.torquelab.autoservice.customer_management.data.remote.CustomerApi
import com.torquelab.autoservice.fleet.data.FleetApi
import com.torquelab.autoservice.staff.data.remote.StaffApi
import com.torquelab.autoservice.staff.data.remote.dto.MechanicDto
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
    private val taskApi: TaskApi,
    private val customerApi: CustomerApi,
    private val fleetApi: FleetApi,
    private val staffApi: StaffApi
) : WorkshopRepository {

    override suspend fun getWorkOrders(): Result<List<WorkshopWorkOrder>> = runCatching {
        val customers = customerApi.getCustomers()
        val vehicles = fleetApi.vehicles()
        val mechanics = staffApi.getMechanics()
        val allTasks = taskApi.getTasks()
        
        workshopApi.getWorkOrders().map { order ->
            val customer = customers.find { it.id == order.customerId.toString() }
            val vehicle = vehicles.find { it.id == order.vehicleId }
            val tasks = allTasks.filter { it.workOrderId == order.id }.map { it.toDomain(mechanics) }
            
            val isRisk = order.status == "PENDING" && order.estimatedDate.isNotEmpty()

            WorkshopWorkOrder(
                id = order.id,
                vehicleId = order.vehicleId,
                customerId = order.customerId,
                trackingCode = order.trackingCode,
                description = order.description,
                status = TaskStatus.fromApi(order.status),
                progress = calculateProgress(tasks),
                tasks = tasks,
                workshopId = order.workshopId,
                customerName = customer?.fullName ?: "---",
                vehiclePlate = vehicle?.plate?.takeIf { it.isNotBlank() } ?: "---",
                startDate = order.startDate,
                estimatedDate = order.estimatedDate,
                calculatedTotal = order.price,
                isRisk = isRisk
            )
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

    private fun TaskDto.toDomain(mechanics: List<MechanicDto>): WorkshopTask {
        val mechanic = mechanics.find { it.id == mechanicId }
        val mechanicName = mechanic?.fullName

        return WorkshopTask(
            id = id,
            orderId = workOrderId,
            description = description,
            priority = priority,
            estimatedMinutes = estimatedTime,
            laborPrice = laborPrice,
            status = TaskStatus.fromApi(status),
            mechanicId = mechanicId,
            mechanicName = mechanicName
        )
    }

    private fun calculateProgress(tasks: List<WorkshopTask>): Int {
        if (tasks.isEmpty()) return 0
        val completed = tasks.count {
            it.status == TaskStatus.COMPLETED || it.status == TaskStatus.DELIVERED
        }
        return (completed * 100) / tasks.size
    }
}
