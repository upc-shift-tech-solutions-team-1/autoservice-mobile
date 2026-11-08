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
import com.torquelab.autoservice.mechanic.domain.model.TaskReport
import retrofit2.HttpException

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
                        error = orderResult.exceptionOrNull()?.message ?: tasksResult.exceptionOrNull()?.message ?: inventoryResult.exceptionOrNull()?.message ?: "Error al cargar la orden.",
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
        mutate { mechanicRepository.startTask(taskId) }
    }

    fun completeTask(taskId: Int) {
        mutate { mechanicRepository.completeTask(taskId, _state.value.diagnosisText) }
    }

    fun proposeTask(input: MechanicProposeTaskInput) {
        mutate { mechanicRepository.proposeTask(input) }
    }

    fun saveReport(taskId: Int, report: TaskReport, complete: Boolean) {
        report.validationError()?.let { error -> _state.update { it.copy(actionError = error) }; return }
        mutate { mechanicRepository.saveTaskReport(taskId, report, complete) }
    }

    private fun mutate(action: suspend () -> Result<Unit>) {
        if (_state.value.isSubmitting) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, actionError = null) }
            action().onSuccess {
                _state.update { it.copy(isSubmitting = false, savedVersion = it.savedVersion + 1) }
                loadOrderData()
            }.onFailure { error ->
                val message = if (error is HttpException && error.code() == 400)
                    "No se pudo realizar la operación. Verifique aprobación, existencias y estado de la tarea."
                    else error.message ?: "No se pudo guardar la tarea."
                _state.update { it.copy(isSubmitting = false, actionError = message) }
            }
        }
    }

    fun getCurrentMechanicId(): Int? {
        return sessionManager.currentSession()?.mechanicId
    }
}
