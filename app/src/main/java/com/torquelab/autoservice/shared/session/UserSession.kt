package com.torquelab.autoservice.shared.session

data class UserSession(
    val userId: Int,
    val email: String,
    val role: String,
    val workshopId: String?,
    val mechanicId: Int?,
    val token: String
)