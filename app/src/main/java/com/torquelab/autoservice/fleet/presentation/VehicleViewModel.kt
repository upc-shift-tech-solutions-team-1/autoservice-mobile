package com.torquelab.autoservice.fleet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.fleet.domain.Vehicle
import com.torquelab.autoservice.fleet.domain.usecase.RegisterVehicleUseCase
import com.torquelab.autoservice.fleet.domain.usecase.UpdateOdometerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface VehicleUiState {
    object Idle : VehicleUiState
    object Loading : VehicleUiState
    data class Success(val vehicle: Vehicle) : VehicleUiState
    data class Error(val message: String) : VehicleUiState
}

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val registerVehicleUseCase: RegisterVehicleUseCase,
    private val updateOdometerUseCase: UpdateOdometerUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleUiState>(VehicleUiState.Idle)
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    fun registerVehicle(vehicle: Vehicle) {
        _uiState.value = VehicleUiState.Loading
        viewModelScope.launch {
            registerVehicleUseCase(vehicle)
                .onSuccess { registered ->
                    _uiState.value = VehicleUiState.Success(registered)
                }
                .onFailure { error ->
                    _uiState.value = VehicleUiState.Error(error.message ?: "Error al registrar vehículo")
                }
        }
    }

    fun updateOdometer(vehicleId: Int, newKilometers: Int) {
        if (updateOdometerUseCase == null) return
        _uiState.value = VehicleUiState.Loading
        viewModelScope.launch {
            updateOdometerUseCase(vehicleId, newKilometers)
                .onSuccess { updated ->
                    _uiState.value = VehicleUiState.Success(updated)
                }
                .onFailure { error ->
                    _uiState.value = VehicleUiState.Error(error.message ?: "Error al actualizar kilometraje")
                }
        }
    }

    fun resetState() {
        _uiState.value = VehicleUiState.Idle
    }
}
