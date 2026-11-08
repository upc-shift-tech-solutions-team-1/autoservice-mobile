package com.torquelab.autoservice.workshop.domain.model

data class WorkshopWorkOrder(
    val id: Int,
    val vehicleId: Int,
    val customerId: Int,
    val trackingCode: String,
    val description: String,
    val status: TaskStatus,
    val progress: Int = 0,
    val tasks: List<WorkshopTask> = emptyList(),
    val workshopId: String = "",
    val customerName: String = "",
    val vehiclePlate: String = "",
    val startDate: String = "",
    val estimatedDate: String = "",
    val calculatedTotal: Double = 0.0,
    val isRisk: Boolean = false,
    val mechanicId: Int = 0,
    val checklist: QualityChecklist = QualityChecklist()
)

data class WorkshopTask(
    val id: Int,
    val orderId: Int,
    val description: String,
    val priority: String,
    val estimatedMinutes: Int,
    val laborPrice: Double,
    val materialsCost: Double,
    val status: TaskStatus,
    val mechanicId: Int?,
    val mechanicName: String? = null,
    val laborCost: Double = 0.0,
    val adminReviewStatus: String = "",
    val parts: List<WorkshopPart> = emptyList(),
    val technicalDiagnosis: String = "",
    val customerExplanation: String = "",
    val evidenceRegistered: String = "",
    val internalObservation: String = ""
)

data class WorkshopPart(val inventoryItemId: Int, val name: String, val quantity: Int, val unitPrice: Double)

data class CreateTaskInput(
    val orderId: Int,
    val description: String,
    val priority: String,
    val estimatedMinutes: Int,
    val laborPrice: Double,
    val mechanicId: Int?,
    val laborCost: Double = 0.0,
    val parts: List<TaskMaterialInput> = emptyList()
)

data class UpdateTaskInput(
    val taskId: Int,
    val description: String,
    val priority: String,
    val estimatedMinutes: Int,
    val laborPrice: Double,
    val mechanicId: Int?,
    val status: TaskStatus,
    val laborCost: Double = 0.0,
    val adminReviewStatus: String? = null
)
