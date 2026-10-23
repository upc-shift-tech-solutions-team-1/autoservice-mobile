package com.torquelab.autoservice.customer_trust.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderNotFoundException
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder
import com.torquelab.autoservice.customer_trust.domain.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingUiState(
    val isLoading: Boolean = false,
    val order: TrackingOrder? = null,
    val error: TrackingUiError? = null,
    val searchCode: String = ""
)

enum class TrackingUiError {
    EMPTY_CODE,
    NOT_FOUND,
    CONNECTION
}

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repository: TrackingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState = _uiState.asStateFlow()

    fun onCodeChange(code: String) {
        _uiState.update { it.copy(searchCode = code, error = null) }
    }

    fun search() {
        val code = _uiState.value.searchCode.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(error = TrackingUiError.EMPTY_CODE, order = null) }
            return
        }

        _uiState.update { it.copy(searchCode = code, isLoading = true, error = null, order = null) }
        viewModelScope.launch {
            repository.getOrderByCode(code)
                .onSuccess { order ->
                    _uiState.update { it.copy(isLoading = false, order = order) }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    val uiError = if (error is TrackingOrderNotFoundException) {
                        TrackingUiError.NOT_FOUND
                    } else {
                        TrackingUiError.CONNECTION
                    }
                    _uiState.update { it.copy(isLoading = false, error = uiError) }
                }
        }
    }

    fun resetSearch() {
        _uiState.update { it.copy(searchCode = "", order = null, error = null) }
    }
}
