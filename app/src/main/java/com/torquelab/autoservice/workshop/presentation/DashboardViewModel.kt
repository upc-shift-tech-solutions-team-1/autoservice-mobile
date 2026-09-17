package com.torquelab.autoservice.workshop.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DashboardUiState(
    val workshopName: String = "",
    val activeWorkOrders: Int = 0,
    val pendingTasks: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load summary data
    }
}
