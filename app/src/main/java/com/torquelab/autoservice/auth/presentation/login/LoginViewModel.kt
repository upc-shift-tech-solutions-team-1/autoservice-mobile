package com.torquelab.autoservice.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.auth.domain.repository.AuthRepository
import com.torquelab.autoservice.auth.presentation.AuthUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                error = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                error = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(
                isPasswordVisible =
                    !it.isPasswordVisible
            )
        }
    }

    fun signIn() {

        if (_uiState.value.isLoading) {
            return
        }

        val email =
            _uiState.value.email.trim()

        val password =
            _uiState.value.password

        if (email.isBlank()) {
            showError(
                AuthUiError.EMAIL_REQUIRED
            )
            return
        }

        if (password.isBlank()) {
            showError(
                AuthUiError.PASSWORD_REQUIRED
            )
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {

            authRepository
                .signIn(
                    email = email,
                    password = password
                )
                .onSuccess {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->

                    val error =
                        mapExceptionToUiError(
                            exception
                        )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                }
        }
    }

    private fun mapExceptionToUiError(
        exception: Throwable
    ): AuthUiError {

        return when (
            exception.message
        ) {

            "Incorrect email or password." ->
                AuthUiError.INVALID_CREDENTIALS

            "Unable to connect to AutoService." ->
                AuthUiError.CONNECTION_ERROR

            "Authentication failed." ->
                AuthUiError.AUTHENTICATION_FAILED

            else ->
                AuthUiError.UNEXPECTED_ERROR
        }
    }

    private fun showError(
        error: AuthUiError
    ) {

        _uiState.update {
            it.copy(
                isLoading = false,
                error = error
            )
        }
    }

    fun consumeLoginSuccess() {

        _uiState.update {
            it.copy(
                isLoginSuccessful = false
            )
        }
    }
}