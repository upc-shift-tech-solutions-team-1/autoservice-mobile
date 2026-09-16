package com.torquelab.autoservice.customer_trust.data.remote.dto

import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder
import com.torquelab.autoservice.customer_trust.domain.model.TrackingTask

data class TrackingOrderDto(
    val trackingCode: String,
    val vehiclePlate: String,
    val vehicleModel: String,
    val status: String,
    val tasks: List<TrackingTaskDto>,
    val estimatedDelivery: String,
    val totalCost: Double,
    val progress: Int
) {
    fun toDomain() = TrackingOrder(
        trackingCode = trackingCode,
        vehiclePlate = vehiclePlate,
        vehicleModel = vehicleModel,
        status = status,
        tasks = tasks.map { it.toDomain() },
        estimatedDelivery = estimatedDelivery,
        totalCost = totalCost,
        progress = progress
    )
}

data class TrackingTaskDto(
    val description: String,
    val status: String
) {
    fun toDomain() = TrackingTask(
        description = description,
        status = status
    )
}
