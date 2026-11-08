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
    val recentOrders: List<WorkshopWorkOrder> = emptyList(),
    val error: String? = null,
    val periodSeries: List<com.torquelab.autoservice.workshop.domain.model.FinancialPeriodPoint> = emptyList(),
    val orderProfitability: List<com.torquelab.autoservice.workshop.domain.model.OrderProfitability> = emptyList()
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val summaryResult = repository.getFinancialSummary()
            val ordersResult = repository.getWorkOrders()
            
            if (ordersResult.isSuccess && summaryResult.isSuccess) {
                val orders = ordersResult.getOrNull() ?: emptyList()
                val summary = summaryResult.getOrNull()
                
                val nonCancelledOrders = orders.filter { it.status != TaskStatus.CANCELLED }
                val activeOrders = nonCancelledOrders.filter { it.status == TaskStatus.IN_PROGRESS || it.status == TaskStatus.PENDING }
                
                val activeVehicles = activeOrders.map { it.vehicleId }.distinct().count()
                val activeCount = activeOrders.size
                val completedCount = nonCancelledOrders.count { it.status == TaskStatus.FINISHED || it.status == TaskStatus.DELIVERED }
                
                val recent = orders.sortedByDescending { it.id }.take(4)

                if (summary != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeVehicles = activeVehicles,
                            activeWorkOrders = activeCount,
                            completedOrders = completedCount,
                            projectedIncome = summary.projectedRevenue,
                            realizedIncome = summary.realizedRevenue,
                            pendingIncome = summary.pendingRevenue,
                            operatingCosts = summary.operatingCost,
                            grossProfit = summary.grossProfit,
                            profitMargin = summary.marginPercentage,
                            averageTicket = summary.averageTicket,
                            profitableOrders = summary.profitableOrders,
                            lossOrders = summary.lossOrders,
                            recentOrders = recent,
                            periodSeries = summary.periodSeries,
                            orderProfitability = summary.orderProfitability
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = summaryResult.exceptionOrNull()?.message ?: ordersResult.exceptionOrNull()?.message ?: "No se pudo cargar el panel.") }
            }
        }
    }
}
