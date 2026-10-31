package com.torquelab.autoservice.mechanic.presentation.workspace

import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.mechanic.domain.repository.MechanicRepository
import com.torquelab.autoservice.shared.session.SessionManager
import com.torquelab.autoservice.shared.session.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MechanicDashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mechanicRepository: MechanicRepository
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mechanicRepository = mock(MechanicRepository::class.java)
        sessionManager = mock(SessionManager::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init fetches orders and sets mechanic email when mechanic session exists`() = runTest {
        val session = UserSession(
            userId = 10,
            email = "carlos.mecanico@autoservice.com",
            role = "ROLE_MECHANIC",
            workshopId = "WS-01",
            mechanicId = 7,
            token = "jwt-token"
        )
        whenever(sessionManager.currentSession()).thenReturn(session)

        val orders = listOf(
            MechanicOrder(
                id = 1,
                trackingCode = "TRK-001",
                vehicleId = 10,
                customerId = 1,
                mechanicId = 7,
                description = "Revisión general",
                status = "IN_PROGRESS",
                price = 150.0,
                vehicleName = "Toyota Hilux",
                vehiclePlate = "ABC-123",
                progress = 60
            )
        )
        whenever(mechanicRepository.getOrdersForMechanic(7)).thenReturn(Result.success(orders))

        val viewModel = MechanicDashboardViewModel(mechanicRepository, sessionManager)

        val state = viewModel.state.value
        assertEquals("carlos.mecanico@autoservice.com", state.mechanicName)
        assertEquals(orders, state.orders)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init does not call repository when session is null`() = runTest {
        whenever(sessionManager.currentSession()).thenReturn(null)

        val viewModel = MechanicDashboardViewModel(mechanicRepository, sessionManager)

        val state = viewModel.state.value
        assertEquals("Mechanic", state.mechanicName)
        assertFalse(state.isLoading)
        verify(mechanicRepository, never()).getOrdersForMechanic(org.mockito.kotlin.any())
    }

    @Test
    fun `fetchOrders updates state with error message when repository fails`() = runTest {
        val session = UserSession(
            userId = 10,
            email = "carlos@autoservice.com",
            role = "ROLE_MECHANIC",
            workshopId = "WS-01",
            mechanicId = 7,
            token = "jwt-token"
        )
        whenever(sessionManager.currentSession()).thenReturn(session)
        whenever(mechanicRepository.getOrdersForMechanic(7))
            .thenReturn(Result.failure(RuntimeException("No se pudo conectar al servidor")))

        val viewModel = MechanicDashboardViewModel(mechanicRepository, sessionManager)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("No se pudo conectar al servidor", state.error)
    }
}
