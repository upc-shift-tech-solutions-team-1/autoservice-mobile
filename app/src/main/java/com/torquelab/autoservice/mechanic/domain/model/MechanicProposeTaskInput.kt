package com.torquelab.autoservice.mechanic.domain.model

data class MechanicProposeTaskInput(
    val workOrderId: Int,
    val mechanicId: Int,
    val description: String,
    val priority: String,
    val estimatedTime: Int,
    val technicalDiagnosis: String,
    val parts: List<MechanicTaskPartInput>
)
