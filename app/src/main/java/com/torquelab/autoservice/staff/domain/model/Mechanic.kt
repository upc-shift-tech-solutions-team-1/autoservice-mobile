package com.torquelab.autoservice.staff.domain.model

data class Mechanic(
    val id: Int,
    val fullName: String,
    val email: String,
    val specialty: String,
    val maxCapacity: Int,
    val assignedTasks: Int,
    val workshopId: String,
    val isAvailable: Boolean
)

data class CreateMechanicInput(
    val fullName: String,
    val email: String,
    val specialty: String,
    val maxCapacity: Int,
    val password: String
)
