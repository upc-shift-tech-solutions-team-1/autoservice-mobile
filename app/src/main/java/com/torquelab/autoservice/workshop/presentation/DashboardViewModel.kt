package com.torquelab.autoservice.workshop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder
import com.torquelab.autoservice.workshop.domain.repository.WorkshopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val workshopName: String = "TorqueLab",
    val activeVehicles: Int = 0,
    val activeWorkOrders: Int = 0,
    val completedOrders: Int = 0,
    val projectedIncome: Double = 0.0,
    val realizedIncome: Double = 0.0,
    val pendingIncome: Double = 0.0,
    val operatingCosts: Double = 0.0,
    val grossProfit: Double = 0.0,
    val profitMargin: Double = 0.0,
    val averageTicket: Double = 0.0,
    val profitableOrders: Int = 0,
    val lossOrders: Int = 0,
    val recentOrders: List<WorkshopWorkOrder> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: WorkshopRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getWorkOrders()
                .onSuccess { orders ->
                    val nonCancelledOrders = orders.filter { it.status != TaskStatus.CANCELLED }
                    val activeOrders = nonCancelledOrders.filter { it.status == TaskStatus.IN_PROGRESS || it.status == TaskStatus.PENDING }
                    
                    val activeVehicles = activeOrders.map { it.vehicleId }.distinct().count()
                    val activeCount = activeOrders.size
                    val completedCount = nonCancelledOrders.count { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.DELIVERED }
                    
                    val allTasks = nonCancelledOrders.flatMap { it.tasks }
                    
                    val projectedIncome = allTasks.sumOf { it.laborPrice + it.materialsCost }
                    
                    val realizedTasks = nonCancelledOrders.filter { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.DELIVERED }.flatMap { it.tasks }
                    val realizedIncome = realizedTasks.sumOf { it.laborPrice + it.materialsCost }
                    
                    val pendingTasks = nonCancelledOrders.filter { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS }.flatMap { it.tasks }
                    val pendingIncome = pendingTasks.sumOf { it.laborPrice + it.materialsCost }
                    
                    // En el prototipo web, los costos operativos a veces son fijos, pero podemos inferirlo de los materiales para el demo.
                    // o simularemos el valor de la web: sum of material costs.
                    val operatingCosts = allTasks.sumOf { it.materialsCost } 
                    
                    val grossProfit = projectedIncome - operatingCosts
                    val profitMargin = if (projectedIncome > 0) (grossProfit / projectedIncome) * 100 else 0.0
                    
                    val validOrdersForTicket = nonCancelledOrders.filter { it.tasks.isNotEmpty() }
                    var profitable = 0
                    var loss = 0
                    validOrdersForTicket.forEach { o ->
                        val rev = o.tasks.sumOf { it.laborPrice + it.materialsCost }
                        val cost = o.tasks.sumOf { it.materialsCost }
                        if (rev - cost >= 0) profitable++ else loss++
                    }
                    val averageTicket = if (validOrdersForTicket.isNotEmpty()) projectedIncome / validOrdersForTicket.size else 0.0
                    
                    val recent = orders.sortedByDescending { it.id }.take(4)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeVehicles = activeVehicles,
                            activeWorkOrders = activeCount,
                            completedOrders = completedCount,
                            projectedIncome = projectedIncome,
                            realizedIncome = realizedIncome,
                            pendingIncome = pendingIncome,
                            operatingCosts = operatingCosts,
                            grossProfit = grossProfit,
                            profitMargin = profitMargin,
                            averageTicket = averageTicket,
                            profitableOrders = profitable,
                            lossOrders = loss,
                            recentOrders = recent
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }
}
