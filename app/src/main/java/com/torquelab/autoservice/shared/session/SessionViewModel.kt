package com.torquelab.autoservice.shared.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(SessionUiState())

    val uiState: StateFlow<SessionUiState> =
        _uiState.asStateFlow()

    fun logout() {

        if (_uiState.value.isLoggingOut) {
            return
        }

        _uiState.update {
            it.copy(
                isLoggingOut = true
            )
        }

        viewModelScope.launch {

            sessionManager.clearSession()

            _uiState.update {
                it.copy(
                    isLoggingOut = false,
                    isLoggedOut = true
                )
            }
        }
    }

    fun consumeLogout() {

        _uiState.update {
            it.copy(
                isLoggedOut = false
            )
        }
    }
}