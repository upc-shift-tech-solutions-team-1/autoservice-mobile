package com.torquelab.autoservice.customer_trust.data.remote.dto

import com.torquelab.autoservice.customer_trust.domain.model.TrackingPart
import com.torquelab.autoservice.customer_trust.domain.model.TrackingTask
import com.torquelab.autoservice.customer_trust.domain.model.TrackingTaskStatus

data class TrackingOrderDto(
    val id: Int,
    val trackingCode: String?,
    val workshopId: String?,
    val vehicleId: Int,
    val customerId: Int,
    val description: String?,
    val status: String?,
    val price: Double?,
    val estimatedDate: String?
)

data class TrackingVehicleDto(
    val brand: String?,
    val model: String?,
    val plate: String?
)

data class TrackingCustomerDto(
    val fullName: String?
)

data class TrackingWorkshopDto(
    val name: String?
)

data class TrackingTaskDto(
    val id: Int,
    val description: String?,
    val status: String?,
    val technicalDiagnosis: String?,
    val customerExplanation: String?,
    val evidenceRegistered: String?,
    val laborPrice: Double?,
    val parts: List<TrackingPartDto>?
) {
    fun toDomain() = TrackingTask(
        id = id,
        description = description.orEmpty(),
        status = TrackingTaskStatus.fromApi(status),
        technicalDiagnosis = technicalDiagnosis,
        customerExplanation = customerExplanation,
        evidenceRegistered = evidenceRegistered,
        laborPrice = laborPrice,
        parts = parts.orEmpty().mapNotNull { it.toDomain() }
    )
}

data class TrackingPartDto(
    val name: String?,
    val quantity: Int?,
    val unitPrice: Double?
) {
    fun toDomain(): TrackingPart? {
        val partName = name?.takeIf { it.isNotBlank() } ?: return null
        return TrackingPart(
            name = partName,
            quantity = quantity ?: return null,
            unitPrice = unitPrice
        )
    }
}
