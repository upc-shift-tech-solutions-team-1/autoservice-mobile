package com.torquelab.autoservice.workshop.presentation

import com.torquelab.autoservice.workshop.domain.model.FinancialSummary
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder
import com.torquelab.autoservice.workshop.domain.repository.WorkshopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: WorkshopRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(WorkshopRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData calculates operational and financial metrics accurately`() = runTest {
        val summary = FinancialSummary(
            projectedRevenue = 20000.0,
            realizedRevenue = 15000.0,
            pendingRevenue = 5000.0,
            operatingCost = 8000.0,
            grossProfit = 7000.0,
            marginPercentage = 46.67,
            profitableOrders = 12,
            lossOrders = 1,
            averageTicket = 1250.0
        )

        val orders = listOf(
            WorkshopWorkOrder(id = 1, vehicleId = 10, customerId = 1, trackingCode = "T1", description = "Order 1", status = TaskStatus.IN_PROGRESS),
            WorkshopWorkOrder(id = 2, vehicleId = 10, customerId = 1, trackingCode = "T2", description = "Order 2", status = TaskStatus.PENDING), // Same vehicle
            WorkshopWorkOrder(id = 3, vehicleId = 20, customerId = 2, trackingCode = "T3", description = "Order 3", status = TaskStatus.COMPLETED),
            WorkshopWorkOrder(id = 4, vehicleId = 30, customerId = 3, trackingCode = "T4", description = "Order 4", status = TaskStatus.DELIVERED),
            WorkshopWorkOrder(id = 5, vehicleId = 40, customerId = 4, trackingCode = "T5", description = "Order 5", status = TaskStatus.CANCELLED),
            WorkshopWorkOrder(id = 6, vehicleId = 50, customerId = 5, trackingCode = "T6", description = "Order 6", status = TaskStatus.IN_PROGRESS)
        )

        whenever(repository.getFinancialSummary()).thenReturn(Result.success(summary))
        whenever(repository.getWorkOrders()).thenReturn(Result.success(orders))

        val viewModel = DashboardViewModel(repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)

        // Active orders are IN_PROGRESS (id=1, id=6) and PENDING (id=2) -> total 3
        assertEquals(3, state.activeWorkOrders)

        // Distinct active vehicles: vehicleId 10 and vehicleId 50 -> total 2
        assertEquals(2, state.activeVehicles)

        // Completed/delivered: id=3 (COMPLETED) and id=4 (DELIVERED) -> total 2
        assertEquals(2, state.completedOrders)

        // Financial summary metrics
        assertEquals(20000.0, state.projectedIncome, 0.001)
        assertEquals(15000.0, state.realizedIncome, 0.001)
        assertEquals(5000.0, state.pendingIncome, 0.001)
        assertEquals(8000.0, state.operatingCosts, 0.001)
        assertEquals(7000.0, state.grossProfit, 0.001)
        assertEquals(46.67, state.profitMargin, 0.001)
        assertEquals(1250.0, state.averageTicket, 0.001)
        assertEquals(12, state.profitableOrders)
        assertEquals(1, state.lossOrders)

        // Recent orders (top 4 sorted descending by id): 6, 5, 4, 3
        assertEquals(4, state.recentOrders.size)
        assertEquals(6, state.recentOrders[0].id)
        assertEquals(5, state.recentOrders[1].id)
        assertEquals(4, state.recentOrders[2].id)
        assertEquals(3, state.recentOrders[3].id)
    }

    @Test
    fun `loadData sets isLoading false when repository returns failure`() = runTest {
        whenever(repository.getFinancialSummary()).thenReturn(Result.failure(RuntimeException("Error en servidor")))
        whenever(repository.getWorkOrders()).thenReturn(Result.success(emptyList()))

        val viewModel = DashboardViewModel(repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.activeWorkOrders)
    }
}
