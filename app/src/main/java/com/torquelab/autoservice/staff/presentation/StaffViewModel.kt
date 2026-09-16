package com.torquelab.autoservice.staff.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.staff.domain.model.CreateMechanicInput
import com.torquelab.autoservice.staff.domain.model.Mechanic
import com.torquelab.autoservice.staff.domain.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StaffUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val mechanics: List<Mechanic> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val repository: StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getMechanics()
                .onSuccess { mechanics ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mechanics = mechanics,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                                ?: "Unable to load staff."
                        )
                    }
                }
        }
    }

    fun registerMechanic(input: CreateMechanicInput) {
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            repository.registerMechanic(input)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false) }
                    load()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = exception.message
                                ?: "Unable to register the mechanic."
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
