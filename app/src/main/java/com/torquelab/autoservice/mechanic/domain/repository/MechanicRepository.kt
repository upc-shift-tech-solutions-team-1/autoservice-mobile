package com.torquelab.autoservice.mechanic.domain.repository

import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTask
import com.torquelab.autoservice.workshop.domain.model.TaskStatus

interface MechanicRepository {
    
    suspend fun getOrdersForMechanic(mechanicId: Int): Result<List<MechanicOrder>>

    suspend fun getOrderById(orderId: Int): Result<MechanicOrder>

    suspend fun getTasksForOrder(orderId: Int): Result<List<MechanicTask>>

    suspend fun getInventoryItems(): Result<List<InventoryItem>>

    suspend fun proposeTask(input: MechanicProposeTaskInput): Result<Unit>

    suspend fun startTask(taskId: Int): Result<Unit>

    suspend fun completeTask(taskId: Int, technicalDiagnosis: String): Result<Unit>
}
