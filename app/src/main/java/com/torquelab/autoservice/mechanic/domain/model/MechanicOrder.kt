package com.torquelab.autoservice.mechanic.domain.model

data class MechanicOrder(
    val id: Int,
    val trackingCode: String,
    val vehicleId: Int,
    val customerId: Int,
    val mechanicId: Int,
    val description: String,
    val status: String,
    val price: Double,
    
    // UI specific formatted fields
    val vehicleName: String = "",
    val tasksCompleted: Int = 0,
    val totalTasks: Int = 0,
    val totalLaborCost: Double = 0.0,
    val totalMaterialsCost: Double = 0.0,
    val progress: Int = 0
)
