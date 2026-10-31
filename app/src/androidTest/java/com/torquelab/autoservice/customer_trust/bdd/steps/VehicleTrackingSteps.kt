
package com.torquelab.autoservice.customer_trust.bdd.steps

import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderNotFoundException
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderStatus
import com.torquelab.autoservice.customer_trust.domain.repository.TrackingRepository
import com.torquelab.autoservice.customer_trust.presentation.TrackingUiError
import com.torquelab.autoservice.customer_trust.presentation.TrackingViewModel
import org.junit.Assert.assertEquals
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull

class VehicleTrackingSteps {

    private val orders = mutableMapOf<String, TrackingOrder>()

    private val repository = object : TrackingRepository {
        override suspend fun getOrderByCode(
            code: String
        ): Result<TrackingOrder> {
            val order = orders[code]

            return if (order != null) {
                Result.success(order)
            } else {
                Result.failure(TrackingOrderNotFoundException())
            }
        }
    }

    private val viewModel = TrackingViewModel(repository)

    fun givenExistingOrder(code: String) {
        orders[code] = TrackingOrder(
            trackingCode = code,
            vehicleBrand = "Toyota",
            vehicleModel = "Corolla",
            vehiclePlate = "ABC-123",
            customerName = "Juan Perez",
            workshopName = "AutoService",
            serviceDescription = "Mantenimiento preventivo",
            status = TrackingOrderStatus.IN_PROGRESS,
            tasks = emptyList(),
            estimatedDelivery = null,
            totalCost = null
        )
    }

    fun givenMissingOrder(code: String) {
        orders.remove(code)
    }

    fun givenCustomerEntersCode(code: String) {
        viewModel.onCodeChange(code)
    }
    suspend fun whenCustomerSearchesTracking() {
        viewModel.search()

        // Espera a que el ViewModel termine de actualizar su estado.
        kotlinx.coroutines.withTimeout(5_000) {
            viewModel.uiState.first { !it.isLoading }
        }
    }

    fun thenOrderIsDisplayed(expectedCode: String) {
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNotNull(state.order)
        assertEquals(expectedCode, state.order?.trackingCode)
    }

    fun thenVehiclePlateIsDisplayed(expectedPlate: String) {
        assertEquals(
            expectedPlate,
            viewModel.uiState.value.order?.vehiclePlate
        )
    }

    fun thenNoErrorIsDisplayed() {
        assertNull(viewModel.uiState.value.error)
    }

    fun thenOrderNotFoundErrorIsDisplayed() {
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(TrackingUiError.NOT_FOUND, state.error)
    }

    fun thenNoOrderIsDisplayed() {
        assertNull(viewModel.uiState.value.order)
    }
}
