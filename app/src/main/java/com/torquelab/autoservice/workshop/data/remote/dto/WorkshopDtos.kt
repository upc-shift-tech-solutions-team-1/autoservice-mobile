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

data class TaskDto(
    val id: Int,
    val workOrderId: Int,
    val mechanicId: Int?,
    val description: String,
    val status: String,
    val priority: String,
    val estimatedTime: Int,
    val laborPrice: Double,
    val technicalDiagnosis: String? = null,
    val customerExplanation: String? = null,
    val internalObservation: String? = null,
    val evidenceRegistered: String? = null,
    val adminReviewStatus: String? = null
)

data class CreateTaskRequest(
    val workOrderId: Int,
    val mechanicId: Int?,
    val description: String,
    val priority: String,
    val estimatedTime: Int,
    val laborPrice: Double
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
