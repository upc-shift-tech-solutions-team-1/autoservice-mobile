package com.torquelab.autoservice.workshop.data.remote.dto

data class WorkOrderDto(
    val id: Int,
    val workshopId: String,
    val trackingCode: String,
    val vehicleId: Int,
    val customerId: Int,
    val mechanicId: Int,
    val description: String,
    val status: String,
    val price: Double,
    val estimatedDate: String,
    val startDate: String,
    val tasksCompleted: Boolean,
    val sparePartsChecked: Boolean,
    val diagnosisValidated: Boolean,
    val cleaningDone: Boolean,
    val finalTestDone: Boolean
)

data class TaskPartDto(
    val id: Int,
    val inventoryItemId: Int,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val purchasePrice: Double,
    val brand: String?,
    val qualityTier: String?
)

data class TaskDto(
    val id: Int,
    val workOrderId: Int,
    val mechanicId: Int?,
    val description: String,
    val status: String,
    val priority: String,
    val estimatedTime: Int,
    val laborPrice: Double,
    val materialsCost: Double? = null,
    val technicalDiagnosis: String? = null,
    val customerExplanation: String? = null,
    val internalObservation: String? = null,
    val evidenceRegistered: String? = null,
    val adminReviewStatus: String? = null,
    val parts: List<TaskPartDto>? = null
)

data class CreateTaskPartRequest(
    val inventoryItemId: Int,
    val quantity: Int
)

data class CreateTaskRequest(
    val workOrderId: Int,
    val mechanicId: Int?,
    val description: String,
    val priority: String,
    val estimatedTime: Int,
    val laborPrice: Double,
    val parts: List<CreateTaskPartRequest>? = null,
    val technicalDiagnosis: String? = null,
    val adminReviewStatus: String? = null
)

data class UpdateTaskRequest(
    val description: String,
    val status: String,
    val priority: String,
    val estimatedTime: Int,
    val laborPrice: Double,
    val mechanicId: Int?
)

data class PatchTaskRequest(
    val status: String? = null,
    val technicalDiagnosis: String? = null,
    val customerExplanation: String? = null,
    val internalObservation: String? = null,
    val evidenceRegistered: String? = null,
    val adminReviewStatus: String? = null
)
