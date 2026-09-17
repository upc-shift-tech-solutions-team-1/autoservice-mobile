package com.torquelab.autoservice.auth.presentation.register

import com.torquelab.autoservice.auth.presentation.AuthUiError

data class RegisterUiState(
    val workshopName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: AuthUiError? = null,
    val isRegistrationSuccessful: Boolean = false
)