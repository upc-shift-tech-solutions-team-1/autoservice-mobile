package com.torquelab.autoservice.workshop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.staff.domain.model.Mechanic
import com.torquelab.autoservice.staff.domain.repository.StaffRepository
import com.torquelab.autoservice.workshop.domain.model.CreateTaskInput
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.UpdateTaskInput
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder
import com.torquelab.autoservice.workshop.domain.repository.WorkshopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.torquelab.autoservice.workshop.domain.model.*

data class WorkshopUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val workOrders: List<WorkshopWorkOrder> = emptyList(),
    val mechanics: List<Mechanic> = emptyList(),
    val errorMessage: String? = null,
    val options: WorkshopOptions = WorkshopOptions(),
    val optionsLoading: Boolean = false,
    val savedVersion: Int = 0
)

@HiltViewModel
class WorkshopViewModel @Inject constructor(
    private val repository: WorkshopRepository,
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkshopUiState())
    val uiState: StateFlow<WorkshopUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val ordersResult = repository.getWorkOrders()
            val mechanicsResult = staffRepository.getMechanics()

            if (ordersResult.isSuccess && mechanicsResult.isSuccess) {
                val mechanics = mechanicsResult.getOrDefault(emptyList())
                val mechanicNames = mechanics.associateBy { it.id }
                val orders = ordersResult.getOrDefault(emptyList()).map { order ->
                    order.copy(
                        tasks = order.tasks.map { task ->
                            task.copy(
                                mechanicName = task.mechanicId?.let { mechanicNames[it]?.fullName }
                            )
                        }
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        workOrders = orders,
                        mechanics = mechanics,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ordersResult.exceptionOrNull()?.message
                            ?: mechanicsResult.exceptionOrNull()?.message
                            ?: "Unable to load workshop data."
                    )
                }
            }
        }
    }

    fun createTask(
        orderId: Int,
        description: String,
        priority: String,
        estimatedMinutes: Int,
        laborPrice: Double,
        mechanicId: Int?
    ) {
        executeMutation {
            repository.createTask(
                CreateTaskInput(
                    orderId = orderId,
                    description = description,
                    priority = priority,
                    estimatedMinutes = estimatedMinutes,
                    laborPrice = laborPrice,
                    mechanicId = mechanicId
                )
            )
        }
    }

    fun updateTask(input: UpdateTaskInput) {
        executeMutation { repository.updateTask(input) }
    }

    fun loadOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(optionsLoading = true, errorMessage = null) }
            repository.getOptions().onSuccess { options ->
                _uiState.update { it.copy(options = options, optionsLoading = false) }
            }.onFailure { error -> _uiState.update { it.copy(optionsLoading = false, errorMessage = error.message ?: "No se pudieron cargar las opciones.") } }
        }
    }

    fun createOrder(input: CreateWorkOrderInput) = executeMutation { repository.createWorkOrder(input) }
    fun updateOrder(order: WorkshopWorkOrder, reason: String?) = executeMutation { repository.updateWorkOrder(order, reason) }
    fun createTaskWithParts(input: CreateTaskInput) = executeMutation { repository.createTask(input) }

    fun cancelTask(taskId: Int) {
        executeMutation { repository.cancelTask(taskId) }
    }

    fun updateTaskStatus(taskId: Int, status: TaskStatus) {
        executeMutation { repository.updateTaskStatus(taskId, status) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun executeMutation(action: suspend () -> Result<Unit>) {
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            action()
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, savedVersion = it.savedVersion + 1) }
                    load()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = exception.message
                                ?: "Unable to update the task."
                        )
                    }
                }
        }
    }
}
