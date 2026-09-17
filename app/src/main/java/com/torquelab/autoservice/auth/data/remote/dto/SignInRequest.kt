package com.torquelab.autoservice.auth.data.remote.dto

data class SignInRequest(
    val email: String,
    val password: String
)