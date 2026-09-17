package com.torquelab.autoservice.workshop.domain.model

data class WorkshopWorkOrder(
    val id: Int,
    val vehicleId: Int,
    val customerId: Int,
    val trackingCode: String,
    val description: String,
    val status: TaskStatus,
    val progress: Int,
    val tasks: List<WorkshopTask>,
    val workshopId: String
)

data class WorkshopTask(
    val id: Int,
    val orderId: Int,
    val description: String,
    val priority: String,
    val estimatedMinutes: Int,
    val laborPrice: Double,
    val status: TaskStatus,
    val mechanicId: Int?,
    val mechanicName: String? = null
)

data class CreateTaskInput(
    val orderId: Int,
    val description: String,
    val priority: String,
    val estimatedMinutes: Int,
    val laborPrice: Double,
    val mechanicId: Int?
)

data class UpdateTaskInput(
    val taskId: Int,
    val description: String,
    val priority: String,
    val estimatedMinutes: Int,
    val laborPrice: Double,
    val mechanicId: Int?,
    val status: TaskStatus
)
