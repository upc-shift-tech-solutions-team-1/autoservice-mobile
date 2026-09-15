package com.torquelab.autoservice.staff.data.remote.dto

data class MechanicDto(
    val id: Int,
    val fullName: String,
    val specialty: String,
    val email: String,
    val maxCapacity: Int,
    val workshopId: String
)

data class CreateMechanicRequest(
    val fullName: String,
    val specialty: String,
    val maxCapacity: Int,
    val email: String,
    val password: String
)

data class UpdateMechanicRequest(
    val fullName: String,
    val specialty: String,
    val maxCapacity: Int,
    val email: String
)
