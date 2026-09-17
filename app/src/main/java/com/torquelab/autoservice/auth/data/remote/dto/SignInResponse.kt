package com.torquelab.autoservice.auth.data.remote.dto

data class SignInResponse(
    val id: Int,
    val email: String,
    val role: String,
    val workshopId: String?,
    val mechanicId: Int?,
    val token: String
)