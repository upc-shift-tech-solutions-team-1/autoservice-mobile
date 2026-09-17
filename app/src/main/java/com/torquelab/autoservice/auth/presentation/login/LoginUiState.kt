package com.torquelab.autoservice.auth.presentation.login

import com.torquelab.autoservice.auth.presentation.AuthUiError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: AuthUiError? = null,
    val isLoginSuccessful: Boolean = false
)