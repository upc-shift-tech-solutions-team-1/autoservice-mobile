
package com.torquelab.autoservice.customer_trust.presentation

import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderNotFoundException
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderStatus
import com.torquelab.autoservice.customer_trust.domain.repository.TrackingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {

    private lateinit var repository: TrackingRepository
    private lateinit var viewModel: TrackingViewModel

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        repository = mock(TrackingRepository::class.java)
        viewModel = TrackingViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createOrder() = TrackingOrder(
        trackingCode = "TRK-100",
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

    @Test
    fun `empty tracking code shows validation error without calling repository`() = runTest {
        // Arrange
        viewModel.onCodeChange("   ")

        // Act
        viewModel.search()

        // Assert
        assertEquals(
            TrackingUiError.EMPTY_CODE,
            viewModel.uiState.value.error
        )
        assertNull(viewModel.uiState.value.order)
        assertFalse(viewModel.uiState.value.isLoading)

        verify(repository, never()).getOrderByCode(any())
    }

    @Test
    fun `valid tracking code loads order successfully`() = runTest {
        // Arrange
        val order = createOrder()

        whenever(repository.getOrderByCode("TRK-100"))
            .thenReturn(Result.success(order))

        viewModel.onCodeChange("  TRK-100  ")

        // Act
        viewModel.search()
        dispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("TRK-100", state.searchCode)
        assertEquals("TRK-100", state.order?.trackingCode)
        assertEquals("Toyota", state.order?.vehicleBrand)
        assertEquals("ABC-123", state.order?.vehiclePlate)

        verify(repository).getOrderByCode("TRK-100")
    }

    @Test
    fun `unknown tracking code shows not found error`() = runTest {
        // Arrange
        whenever(repository.getOrderByCode("TRK-404"))
            .thenReturn(
                Result.failure(TrackingOrderNotFoundException())
            )

        viewModel.onCodeChange("TRK-404")

        // Act
        viewModel.search()
        dispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(TrackingUiError.NOT_FOUND, state.error)
        assertNull(state.order)

        verify(repository).getOrderByCode("TRK-404")
    }

    @Test
    fun `connection failure shows connection error`() = runTest {
        // Arrange
        whenever(repository.getOrderByCode("TRK-500"))
            .thenReturn(
                Result.failure(
                    java.io.IOException("Sin conexion al servidor")
                )
            )

        viewModel.onCodeChange("TRK-500")

        // Act
        viewModel.search()
        dispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(TrackingUiError.CONNECTION, state.error)
        assertNull(state.order)

        verify(repository).getOrderByCode("TRK-500")
    }
}
