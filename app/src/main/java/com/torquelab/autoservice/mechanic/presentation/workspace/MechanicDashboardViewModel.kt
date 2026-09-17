package com.torquelab.autoservice.mechanic.presentation.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.mechanic.domain.repository.MechanicRepository
import com.torquelab.autoservice.shared.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MechanicDashboardViewModel @Inject constructor(
    private val mechanicRepository: MechanicRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(MechanicDashboardState())
    val state = _state.asStateFlow()

    init {
        val session = sessionManager.currentSession()
        _state.update { it.copy(mechanicName = session?.email ?: "Mechanic") }
        fetchOrders()
    }

    fun fetchOrders() {
        val session = sessionManager.currentSession()
        val mechanicId = session?.mechanicId ?: return

        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            mechanicRepository.getOrdersForMechanic(mechanicId)
                .onSuccess { orders ->
                    _state.update { it.copy(orders = orders, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }
}
