package com.torquelab.autoservice.mechanic.data.repository

import com.torquelab.autoservice.fleet.data.FleetApi
import com.torquelab.autoservice.inventory.data.InventoryApi
import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.mechanic.data.mapper.toMechanicTask
import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTask
import com.torquelab.autoservice.mechanic.domain.repository.MechanicRepository
import com.torquelab.autoservice.workshop.data.remote.TaskApi
import com.torquelab.autoservice.workshop.data.remote.WorkshopApi
import com.torquelab.autoservice.workshop.data.remote.dto.CreateTaskPartRequest
import com.torquelab.autoservice.workshop.data.remote.dto.CreateTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.PatchTaskRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MechanicRepositoryImpl @Inject constructor(
    private val workshopApi: WorkshopApi,
    private val taskApi: TaskApi,
    private val fleetApi: FleetApi,
    private val inventoryApi: InventoryApi
) : MechanicRepository {

    override suspend fun getOrdersForMechanic(mechanicId: Int): Result<List<MechanicOrder>> = withContext(Dispatchers.IO) {
        try {
            val orders = workshopApi.getWorkOrders()
                .filter { it.mechanicId == mechanicId }
            
            val vehicles = fleetApi.vehicles()
            val tasks = taskApi.getTasks(mechanicId = mechanicId)

            val mappedOrders = orders.map { order ->
                val vehicle = vehicles.find { it.id == order.vehicleId }
                val orderTasks = tasks.filter { it.workOrderId == order.id }

                val completedTasks = orderTasks.count { it.status == "COMPLETED" }
                val totalTasks = orderTasks.size
                
                val progress = if (totalTasks > 0) {
                    (completedTasks.toDouble() / totalTasks * 100).toInt()
                } else {
                    0
                }

                val totalLaborCost = orderTasks.sumOf { it.laborPrice }
                val totalMaterialsCost = orderTasks.sumOf { it.materialsCost ?: 0.0 }

                MechanicOrder(
                    id = order.id,
                    trackingCode = order.trackingCode,
                    vehicleId = order.vehicleId,
                    customerId = order.customerId,
                    mechanicId = order.mechanicId,
                    description = order.description,
                    status = order.status,
                    price = order.price,
                    vehicleName = "${vehicle?.brand ?: "Vehículo"} ${vehicle?.model ?: ""}".trim(),
                    tasksCompleted = completedTasks,
                    totalTasks = totalTasks,
                    totalLaborCost = totalLaborCost,
                    totalMaterialsCost = totalMaterialsCost,
                    progress = progress
                )
            }
            Result.success(mappedOrders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(orderId: Int): Result<MechanicOrder> = withContext(Dispatchers.IO) {
        try {
            val order = workshopApi.getWorkOrder(orderId)
            val vehicles = fleetApi.vehicles()
            val tasks = taskApi.getTasks(workOrderId = orderId)

            val vehicle = vehicles.find { it.id == order.vehicleId }
            val completedTasks = tasks.count { it.status == "COMPLETED" }
            val totalTasks = tasks.size
            
            val progress = if (totalTasks > 0) {
                (completedTasks.toDouble() / totalTasks * 100).toInt()
            } else {
                0
            }

            val totalLaborCost = tasks.sumOf { it.laborPrice }
            val totalMaterialsCost = tasks.sumOf { it.materialsCost ?: 0.0 }

            val mappedOrder = MechanicOrder(
                id = order.id,
                trackingCode = order.trackingCode,
                vehicleId = order.vehicleId,
                customerId = order.customerId,
                mechanicId = order.mechanicId,
                description = order.description,
                status = order.status,
                price = order.price,
                vehicleName = "${vehicle?.brand ?: "Vehículo"} ${vehicle?.model ?: ""}".trim(),
                tasksCompleted = completedTasks,
                totalTasks = totalTasks,
                totalLaborCost = totalLaborCost,
                totalMaterialsCost = totalMaterialsCost,
                progress = progress
            )
            Result.success(mappedOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTasksForOrder(orderId: Int): Result<List<MechanicTask>> = withContext(Dispatchers.IO) {
        try {
            val tasksDto = taskApi.getTasks(workOrderId = orderId)
            val mappedTasks = tasksDto.map { it.toMechanicTask() }
            Result.success(mappedTasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getInventoryItems(): Result<List<InventoryItem>> = withContext(Dispatchers.IO) {
        try {
            val items = inventoryApi.items()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun proposeTask(input: MechanicProposeTaskInput): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = CreateTaskRequest(
                workOrderId = input.workOrderId,
                mechanicId = input.mechanicId,
                description = input.description,
                priority = input.priority,
                estimatedTime = input.estimatedTime,
                laborPrice = 0.0,
                technicalDiagnosis = input.technicalDiagnosis,
                adminReviewStatus = "SUBMITTED",
                parts = input.parts.map { 
                    CreateTaskPartRequest(
                        inventoryItemId = it.inventoryItemId,
                        quantity = it.quantity
                    ) 
                }
            )
            taskApi.createTask(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startTask(taskId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskApi.patchTask(taskId, PatchTaskRequest(status = "IN_PROGRESS"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeTask(taskId: Int, technicalDiagnosis: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskApi.patchTask(
                id = taskId,
                request = PatchTaskRequest(
                    status = "COMPLETED",
                    technicalDiagnosis = technicalDiagnosis
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
