package com.torquelab.autoservice.customer_trust.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder
import com.torquelab.autoservice.customer_trust.domain.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingUiState(
    val isLoading: Boolean = false,
    val order: TrackingOrder? = null,
    val error: String? = null,
    val searchCode: String = ""
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repository: TrackingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState = _uiState.asStateFlow()

    fun onCodeChange(code: String) {
        _uiState.update { it.copy(searchCode = code) }
    }

    fun search() {
        val code = _uiState.value.searchCode
        if (code.isBlank()) return

        _uiState.update { it.copy(isLoading = true, error = null, order = null) }
        viewModelScope.launch {
            repository.getOrderByCode(code)
                .onSuccess { order ->
                    _uiState.update { it.copy(isLoading = false, order = order) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}
