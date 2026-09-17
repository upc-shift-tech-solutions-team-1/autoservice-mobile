package com.torquelab.autoservice.mechanic.domain.model

import com.torquelab.autoservice.workshop.domain.model.TaskStatus

data class MechanicTask(
    val id: Int,
    val workOrderId: Int,
    val description: String,
    val status: TaskStatus,
    val priority: String,
    val estimatedTime: Int,
    val laborPrice: Double,
    val materialsCost: Double,
    val technicalDiagnosis: String,
    val adminReviewStatus: String,
    val parts: List<MechanicTaskPart>
)
