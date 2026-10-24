package com.torquelab.autoservice.test_doubles

import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTask
import com.torquelab.autoservice.mechanic.domain.repository.MechanicRepository
import com.torquelab.autoservice.workshop.domain.model.CreateTaskInput
import com.torquelab.autoservice.workshop.domain.model.FinancialSummary
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.UpdateTaskInput
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder
import com.torquelab.autoservice.workshop.domain.repository.WorkshopRepository

/**
 * In-memory test double for MechanicRepository used in BDD and Android instrumentation tests.
 */
class FakeMechanicRepository : MechanicRepository {
    val orders = mutableListOf<MechanicOrder>()
    val tasks = mutableListOf<MechanicTask>()
    val inventory = mutableListOf<InventoryItem>()
    var lastProposedInput: MechanicProposeTaskInput? = null
    val startedTaskIds = mutableListOf<Int>()
    val completedTasks = mutableMapOf<Int, String>()

    override suspend fun getOrdersForMechanic(mechanicId: Int): Result<List<MechanicOrder>> =
        Result.success(orders.filter { it.mechanicId == mechanicId })

    override suspend fun getOrderById(orderId: Int): Result<MechanicOrder> =
        orders.find { it.id == orderId }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Order $orderId not found"))

    override suspend fun getTasksForOrder(orderId: Int): Result<List<MechanicTask>> =
        Result.success(tasks.filter { it.workOrderId == orderId })

    override suspend fun getInventoryItems(): Result<List<InventoryItem>> =
        Result.success(inventory)

    override suspend fun proposeTask(input: MechanicProposeTaskInput): Result<Unit> {
        lastProposedInput = input
        return Result.success(Unit)
    }

    override suspend fun startTask(taskId: Int): Result<Unit> {
        startedTaskIds.add(taskId)
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(status = TaskStatus.IN_PROGRESS)
        }
        return Result.success(Unit)
    }

    override suspend fun completeTask(taskId: Int, technicalDiagnosis: String): Result<Unit> {
        completedTasks[taskId] = technicalDiagnosis
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(
                status = TaskStatus.COMPLETED,
                technicalDiagnosis = technicalDiagnosis
            )
        }
        return Result.success(Unit)
    }
}

/**
 * In-memory test double for WorkshopRepository used in BDD and Android instrumentation tests.
 */
class FakeWorkshopRepository : WorkshopRepository {
    val workOrders = mutableListOf<WorkshopWorkOrder>()
    var financialSummary = FinancialSummary(
        projectedRevenue = 25000.0,
        realizedRevenue = 18000.0,
        pendingRevenue = 7000.0,
        operatingCost = 10000.0,
        grossProfit = 8000.0,
        marginPercentage = 44.44,
        profitableOrders = 15,
        lossOrders = 1,
        averageTicket = 1200.0
    )
    val createdTasks = mutableListOf<CreateTaskInput>()
    val updatedTasks = mutableListOf<UpdateTaskInput>()
    val cancelledTaskIds = mutableListOf<Int>()
    val patchedStatuses = mutableMapOf<Int, TaskStatus>()

    override suspend fun getWorkOrders(): Result<List<WorkshopWorkOrder>> =
        Result.success(workOrders)

    override suspend fun getFinancialSummary(): Result<FinancialSummary> =
        Result.success(financialSummary)

    override suspend fun createTask(input: CreateTaskInput): Result<Unit> {
        createdTasks.add(input)
        return Result.success(Unit)
    }

    override suspend fun updateTask(input: UpdateTaskInput): Result<Unit> {
        updatedTasks.add(input)
        return Result.success(Unit)
    }

    override suspend fun cancelTask(taskId: Int): Result<Unit> {
        cancelledTaskIds.add(taskId)
        return Result.success(Unit)
    }

    override suspend fun updateTaskStatus(taskId: Int, status: TaskStatus): Result<Unit> {
        patchedStatuses[taskId] = status
        return Result.success(Unit)
    }
}
