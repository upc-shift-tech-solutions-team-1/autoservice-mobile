package com.torquelab.autoservice.auth.data.remote.dto

data class RegisterWorkshopResponse(
    val message: String,
    val workshopId: String,
    val userId: Int?
)