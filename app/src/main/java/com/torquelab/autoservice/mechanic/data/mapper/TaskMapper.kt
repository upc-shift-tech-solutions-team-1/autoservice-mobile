package com.torquelab.autoservice.mechanic.data.mapper

import com.torquelab.autoservice.mechanic.domain.model.MechanicTask
import com.torquelab.autoservice.mechanic.domain.model.MechanicTaskPart
import com.torquelab.autoservice.workshop.data.remote.dto.TaskDto
import com.torquelab.autoservice.workshop.domain.model.TaskStatus

fun TaskDto.toMechanicTask(): MechanicTask {
    return MechanicTask(
        id = id,
        workOrderId = workOrderId,
        description = description,
        status = TaskStatus.fromApi(status),
        priority = priority,
        estimatedTime = estimatedTime,
        laborPrice = laborPrice,
        materialsCost = materialsCost ?: 0.0,
        technicalDiagnosis = technicalDiagnosis ?: "",
        adminReviewStatus = adminReviewStatus ?: "",
        parts = parts?.map { 
            MechanicTaskPart(
                inventoryItemId = it.inventoryItemId,
                name = it.name,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                purchasePrice = it.purchasePrice,
                brand = it.brand ?: "",
                qualityTier = it.qualityTier ?: ""
            ) 
        } ?: emptyList()
    )
}
