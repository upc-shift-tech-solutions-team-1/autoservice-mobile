package com.torquelab.autoservice.auth.presentation.register

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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(RegisterUiState())

    val uiState: StateFlow<RegisterUiState> =
        _uiState.asStateFlow()

    fun onWorkshopNameChange(value: String) {
        _uiState.update {
            it.copy(
                workshopName = value,
                error = null
            )
        }
    }

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

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
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

    fun toggleConfirmPasswordVisibility() {
        _uiState.update {
            it.copy(
                isConfirmPasswordVisible =
                    !it.isConfirmPasswordVisible
            )
        }
    }

    fun registerWorkshop() {

        if (_uiState.value.isLoading) {
            return
        }

        val state = _uiState.value

        val workshopName =
            state.workshopName.trim()

        val email =
            state.email.trim()

        val password =
            state.password

        val confirmPassword =
            state.confirmPassword

        if (workshopName.isBlank()) {
            showError(
                AuthUiError.WORKSHOP_NAME_REQUIRED
            )
            return
        }

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

        if (password.length < 6) {
            showError(
                AuthUiError.PASSWORD_TOO_SHORT
            )
            return
        }

        if (password != confirmPassword) {
            showError(
                AuthUiError.PASSWORDS_DO_NOT_MATCH
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
                .registerWorkshop(
                    workshopName = workshopName,
                    email = email,
                    password = password
                )
                .onSuccess {

                    signInAfterRegistration(
                        email = email,
                        password = password
                    )
                }
                .onFailure { exception ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error =
                                mapExceptionToUiError(
                                    exception
                                )
                        )
                    }
                }
        }
    }

    private suspend fun signInAfterRegistration(
        email: String,
        password: String
    ) {

        authRepository
            .signIn(
                email = email,
                password = password
            )
            .onSuccess {

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        isRegistrationSuccessful = true
                    )
                }
            }
            .onFailure { exception ->

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error =
                            mapExceptionToUiError(
                                exception
                            )
                    )
                }
            }
    }

    private fun mapExceptionToUiError(
        exception: Throwable
    ): AuthUiError {

        return when (
            exception.message
        ) {

            "Unable to connect to AutoService." ->
                AuthUiError.CONNECTION_ERROR

            "Workshop registration failed." ->
                AuthUiError.REGISTRATION_FAILED

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

    fun consumeRegistrationSuccess() {

        _uiState.update {
            it.copy(
                isRegistrationSuccessful = false
            )
        }
    }
}