package com.torquelab.autoservice.mechanic.presentation.order_execution

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTask
import com.torquelab.autoservice.mechanic.domain.repository.MechanicRepository
import com.torquelab.autoservice.shared.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MechanicOrderExecutionViewModel @Inject constructor(
    private val mechanicRepository: MechanicRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: Int = checkNotNull(savedStateHandle["orderId"])

    private val _state = MutableStateFlow(MechanicOrderExecutionState())
    val state = _state.asStateFlow()

    init {
        loadOrderData()
    }

    fun loadOrderData() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val orderResult = mechanicRepository.getOrderById(orderId)
            val tasksResult = mechanicRepository.getTasksForOrder(orderId)
            val inventoryResult = mechanicRepository.getInventoryItems()

            if (orderResult.isSuccess && tasksResult.isSuccess && inventoryResult.isSuccess) {
                _state.update { 
                    it.copy(
                        order = orderResult.getOrNull(),
                        tasks = tasksResult.getOrNull() ?: emptyList(),
                        inventoryItems = inventoryResult.getOrNull() ?: emptyList(),
                        isLoading = false
                    ) 
                }
            } else {
                _state.update { 
                    it.copy(
                        error = orderResult.exceptionOrNull()?.message ?: "Error loading data",
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun updateDiagnosisText(text: String) {
        _state.update { it.copy(diagnosisText = text) }
    }

    fun startTask(taskId: Int) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            mechanicRepository.startTask(taskId)
            loadOrderData()
        }
    }

    fun completeTask(taskId: Int) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            mechanicRepository.completeTask(taskId, _state.value.diagnosisText)
            loadOrderData()
        }
    }

    fun proposeTask(input: MechanicProposeTaskInput) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            mechanicRepository.proposeTask(input)
            loadOrderData()
        }
    }

    fun getCurrentMechanicId(): Int? {
        return sessionManager.currentSession()?.mechanicId
    }
}
