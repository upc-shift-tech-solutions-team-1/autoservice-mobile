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
import com.torquelab.autoservice.shared.network.apiResult
import com.torquelab.autoservice.workshop.domain.model.*
import com.torquelab.autoservice.workshop.data.remote.dto.CreateTaskPartRequest
import com.torquelab.autoservice.workshop.data.remote.dto.CreateWorkOrderRequest
import com.torquelab.autoservice.workshop.data.remote.dto.UpdateWorkOrderRequest
import com.torquelab.autoservice.inventory.data.InventoryApi
import java.time.LocalDate

@Singleton
class WorkshopRepositoryImpl @Inject constructor(
    private val workshopApi: WorkshopApi,
    private val taskApi: TaskApi,
    private val customerApi: CustomerApi,
    private val fleetApi: FleetApi,
    private val staffApi: StaffApi,
    private val inventoryApi: InventoryApi
) : WorkshopRepository {

    override suspend fun getOptions(): Result<WorkshopOptions> = apiResult {
        WorkshopOptions(fleetApi.vehicles(), fleetApi.owners(), inventoryApi.items())
    }

    override suspend fun createWorkOrder(input: CreateWorkOrderInput): Result<Unit> = apiResult {
        require(input.vehicleId > 0 && input.customerId > 0 && input.mechanicId > 0) { "Seleccione vehículo y mecánico." }
        require(input.description.isNotBlank()) { "Ingrese una descripción." }
        validateEstimatedDate(null, input.estimatedDate, null)?.let { error(it) }
        workshopApi.createWorkOrder(CreateWorkOrderRequest(input.vehicleId, input.customerId, input.mechanicId, input.description.trim(), input.estimatedDate))
        Unit
    }

    override suspend fun updateWorkOrder(order: WorkshopWorkOrder, delayReason: String?): Result<Unit> = apiResult {
        // Read the current server price; a date/checklist edit must not replace it with a stale UI total.
        val current = workshopApi.getWorkOrder(order.id)
        validateEstimatedDate(current.estimatedDate, order.estimatedDate, delayReason)?.let { error(it) }
        val checks = order.checklist
        workshopApi.updateWorkOrder(order.id, UpdateWorkOrderRequest(
            order.description.trim(), order.estimatedDate, current.price, order.status.toApiValue(),
            checks.tasksCompleted, checks.sparePartsChecked, checks.diagnosisValidated,
            checks.cleaningDone, checks.finalTestDone, delayReason?.trim()?.takeIf(String::isNotEmpty)
        ))
        Unit
    }

    override suspend fun getWorkOrders(): Result<List<WorkshopWorkOrder>> = apiResult {
        val customers = customerApi.getCustomers()
        val vehicles = fleetApi.vehicles()
        val mechanics = staffApi.getMechanics()
        val allTasks = taskApi.getTasks()

        workshopApi.getWorkOrders().map { order ->
            val customer = customers.find { it.id == order.customerId.toString() }
            val vehicle = vehicles.find { it.id == order.vehicleId }
            val tasks = allTasks.filter { it.workOrderId == order.id }.map { it.toDomain(mechanics) }

            val isRisk = order.status == "PENDING" && runCatching {
                !LocalDate.parse(order.estimatedDate.take(10)).isAfter(LocalDate.now().plusDays(3))
            }.getOrDefault(false)

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
                isRisk = isRisk,
                mechanicId = order.mechanicId,
                checklist = QualityChecklist(order.tasksCompleted, order.sparePartsChecked, order.diagnosisValidated, order.cleaningDone, order.finalTestDone)
            )
        }
    }

    override suspend fun getFinancialSummary(): Result<com.torquelab.autoservice.workshop.domain.model.FinancialSummary> = apiResult {
        workshopApi.getFinancialSummary().toDomain()
    }

    override suspend fun createTask(input: CreateTaskInput): Result<Unit> = apiResult {
        taskApi.createTask(
            CreateTaskRequest(
                workOrderId = input.orderId,
                mechanicId = input.mechanicId,
                description = input.description,
                priority = input.priority,
                estimatedTime = input.estimatedMinutes,
                laborPrice = input.laborPrice,
                laborCost = input.laborCost,
                parts = input.parts.map { CreateTaskPartRequest(it.inventoryItemId, it.quantity) },
                adminReviewStatus = "APPROVED"
            )
        )
        Unit
    }

    override suspend fun updateTask(input: UpdateTaskInput): Result<Unit> = apiResult {
        taskApi.updateTask(
            id = input.taskId,
            request = UpdateTaskRequest(
                description = input.description,
                status = input.status.toApiValue(),
                priority = input.priority,
                estimatedTime = input.estimatedMinutes,
                laborPrice = input.laborPrice,
                mechanicId = input.mechanicId,
                laborCost = input.laborCost,
                adminReviewStatus = input.adminReviewStatus
            )
        )
        Unit
    }

    override suspend fun cancelTask(taskId: Int): Result<Unit> = apiResult {
        taskApi.deleteTask(taskId)
        Unit
    }

    override suspend fun updateTaskStatus(
        taskId: Int,
        status: TaskStatus
    ): Result<Unit> = apiResult {
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
            materialsCost = materialsCost ?: 0.0,
            status = TaskStatus.fromApi(status),
            mechanicId = mechanicId,
            mechanicName = mechanicName,
            laborCost = laborCost,
            adminReviewStatus = adminReviewStatus.orEmpty(),
            parts = parts.orEmpty().map { WorkshopPart(it.inventoryItemId, it.name, it.quantity, it.unitPrice) },
            technicalDiagnosis = technicalDiagnosis.orEmpty(),
            customerExplanation = customerExplanation.orEmpty(),
            evidenceRegistered = evidenceRegistered.orEmpty(),
            internalObservation = internalObservation.orEmpty()
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
