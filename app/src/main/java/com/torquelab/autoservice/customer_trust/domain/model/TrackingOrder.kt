package com.torquelab.autoservice.customer_trust.domain.model

data class TrackingOrder(
    val trackingCode: String,
    val vehiclePlate: String,
    val vehicleModel: String,
    val status: String,
    val tasks: List<TrackingTask>,
    val estimatedDelivery: String,
    val totalCost: Double,
    val progress: Int
)

data class TrackingTask(
    val description: String,
    val status: String
)
