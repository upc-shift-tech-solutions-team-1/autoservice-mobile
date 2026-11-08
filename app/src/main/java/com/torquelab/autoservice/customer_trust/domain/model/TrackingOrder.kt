package com.torquelab.autoservice.customer_trust.domain.model

data class TrackingOrder(
    val trackingCode: String,
    val vehicleBrand: String?,
    val vehicleModel: String?,
    val vehiclePlate: String?,
    val customerName: String?,
    val workshopName: String?,
    val serviceDescription: String?,
    val status: TrackingOrderStatus,
    val tasks: List<TrackingTask>,
    val estimatedDelivery: String?,
    val totalCost: Double?,
    val reportedProgress: Int? = null,
    val costs: TrackingCosts? = null,
    val history: List<TrackingStage> = emptyList(),
    val estimatedDateHistory: List<TrackingDateChange> = emptyList(),
    val primaryMechanic: TrackingMechanic? = null
) {
    val progress: Int
        get() = reportedProgress?.coerceIn(0, 100) ?: calculateTrackingProgress(status, tasks)
}

data class TrackingCosts(val laborSubtotal: Double, val materialsSubtotal: Double, val total: Double)
data class TrackingStage(val status: String, val changedAtUtc: String)
data class TrackingDateChange(
    val previousEstimatedDate: String?, val estimatedDate: String,
    val reason: String?, val changedAtUtc: String
)
data class TrackingMechanic(val fullName: String, val specialty: String)

data class TrackingTask(
    val id: Int,
    val description: String,
    val status: TrackingTaskStatus,
    val technicalDiagnosis: String?,
    val customerExplanation: String?,
    val evidenceRegistered: String?,
    val laborPrice: Double?,
    val parts: List<TrackingPart>
)

data class TrackingPart(
    val name: String,
    val quantity: Int,
    val unitPrice: Double?
)

enum class TrackingOrderStatus {
    PENDING,
    IN_PROGRESS,
    FINISHED,
    DELIVERED,
    CANCELLED,
    UNKNOWN;

    companion object {
        fun fromApi(value: String?): TrackingOrderStatus = when (value?.trim()?.uppercase()) {
            "PENDING" -> PENDING
            "IN_PROGRESS", "IN PROGRESS" -> IN_PROGRESS
            "FINISHED" -> FINISHED
            "DELIVERED" -> DELIVERED
            "CANCELLED", "CANCELED" -> CANCELLED
            else -> UNKNOWN
        }
    }
}

enum class TrackingTaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    DELIVERED,
    CANCELLED,
    UNKNOWN;

    companion object {
        fun fromApi(value: String?): TrackingTaskStatus = when (value?.trim()?.uppercase()) {
            "PENDING" -> PENDING
            "IN_PROGRESS", "IN PROGRESS" -> IN_PROGRESS
            "COMPLETED" -> COMPLETED
            "DELIVERED" -> DELIVERED
            "CANCELLED", "CANCELED" -> CANCELLED
            else -> UNKNOWN
        }
    }
}

class TrackingOrderNotFoundException : Exception()
