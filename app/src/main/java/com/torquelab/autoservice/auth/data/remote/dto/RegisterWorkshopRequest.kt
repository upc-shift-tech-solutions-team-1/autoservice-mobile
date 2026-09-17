package com.torquelab.autoservice.auth.data.remote.dto

data class RegisterWorkshopRequest(
    val workshopName: String,
    val email: String,
    val password: String
)