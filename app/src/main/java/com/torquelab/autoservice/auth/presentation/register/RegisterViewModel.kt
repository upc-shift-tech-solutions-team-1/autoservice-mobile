package com.torquelab.autoservice.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.auth.domain.repository.AuthRepository
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

    private val _uiState = MutableStateFlow(RegisterUiState())

    val uiState: StateFlow<RegisterUiState> =
        _uiState.asStateFlow()

    fun onWorkshopNameChange(value: String) {
        _uiState.update {
            it.copy(
                workshopName = value,
                errorMessage = null
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                errorMessage = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                errorMessage = null
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(
                isPasswordVisible = !it.isPasswordVisible
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
            showError("Workshop name is required.")
            return
        }

        if (email.isBlank()) {
            showError("Email is required.")
            return
        }

        if (password.isBlank()) {
            showError("Password is required.")
            return
        }

        if (password.length < 6) {
            showError(
                "Password must contain at least 6 characters."
            )
            return
        }

        if (password != confirmPassword) {
            showError("Passwords do not match.")
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {

            val registrationResult =
                authRepository.registerWorkshop(
                    workshopName = workshopName,
                    email = email,
                    password = password
                )

            registrationResult
                .onSuccess {

                    // Same behavior as the Web application:
                    // register workshop and immediately sign in.
                    val loginResult =
                        authRepository.signIn(
                            email = email,
                            password = password
                        )

                    loginResult
                        .onSuccess {

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRegistrationSuccessful = true,
                                    errorMessage = null
                                )
                            }
                        }
                        .onFailure { exception ->

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage =
                                        exception.message
                                            ?: "Workshop created, but automatic sign in failed."
                                )
                            }
                        }
                }
                .onFailure { exception ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                exception.message
                                    ?: "Workshop registration failed."
                        )
                    }
                }
        }
    }

    private fun showError(
        message: String
    ) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message
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