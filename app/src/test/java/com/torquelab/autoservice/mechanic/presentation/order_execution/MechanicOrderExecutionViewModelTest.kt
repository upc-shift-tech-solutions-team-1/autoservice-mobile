package com.torquelab.autoservice.mechanic.presentation.order_execution

import androidx.lifecycle.SavedStateHandle
import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTask
import com.torquelab.autoservice.mechanic.domain.repository.MechanicRepository
import com.torquelab.autoservice.shared.session.SessionManager
import com.torquelab.autoservice.shared.session.UserSession
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
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
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MechanicOrderExecutionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mechanicRepository: MechanicRepository
    private lateinit var sessionManager: SessionManager
    private val orderId = 10

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

    private fun createViewModel(): MechanicOrderExecutionViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("orderId" to orderId))
        return MechanicOrderExecutionViewModel(mechanicRepository, sessionManager, savedStateHandle)
    }

    @Test
    fun `loadOrderData populates order, tasks, and inventory items successfully`() = runTest {
        val mockOrder = MechanicOrder(
            id = orderId,
            trackingCode = "TRK-010",
            vehicleId = 1,
            customerId = 2,
            mechanicId = 5,
            description = "Frenos y suspensión",
            status = "IN_PROGRESS",
            price = 300.0,
            vehicleName = "Toyota Yaris",
            vehiclePlate = "YAR-100"
        )
        val mockTasks = listOf(
            MechanicTask(
                id = 101,
                workOrderId = orderId,
                description = "Rectificado de discos",
                status = TaskStatus.IN_PROGRESS,
                priority = "HIGH",
                estimatedTime = 60,
                laborPrice = 80.0,
                materialsCost = 0.0,
                technicalDiagnosis = "",
                adminReviewStatus = "APPROVED",
                parts = emptyList()
            )
        )
        val mockInventory = listOf(
            InventoryItem(id = 1, name = "Líquido de frenos DOT4", stock = 10)
        )

        whenever(mechanicRepository.getOrderById(orderId)).thenReturn(Result.success(mockOrder))
        whenever(mechanicRepository.getTasksForOrder(orderId)).thenReturn(Result.success(mockTasks))
        whenever(mechanicRepository.getInventoryItems()).thenReturn(Result.success(mockInventory))

        val viewModel = createViewModel()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(mockOrder, state.order)
        assertEquals(mockTasks, state.tasks)
        assertEquals(mockInventory, state.inventoryItems)
    }

    @Test
    fun `loadOrderData sets error message when repository fails`() = runTest {
        whenever(mechanicRepository.getOrderById(orderId))
            .thenReturn(Result.failure(RuntimeException("Orden no encontrada")))
        whenever(mechanicRepository.getTasksForOrder(orderId)).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.getInventoryItems()).thenReturn(Result.success(emptyList()))

        val viewModel = createViewModel()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Orden no encontrada", state.error)
    }

    @Test
    fun `updateDiagnosisText modifies diagnosisText in state`() = runTest {
        whenever(mechanicRepository.getOrderById(orderId)).thenReturn(Result.success(mock(MechanicOrder::class.java)))
        whenever(mechanicRepository.getTasksForOrder(orderId)).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.getInventoryItems()).thenReturn(Result.success(emptyList()))

        val viewModel = createViewModel()

        viewModel.updateDiagnosisText("Pastillas con 2mm de espesor restante")

        assertEquals("Pastillas con 2mm de espesor restante", viewModel.state.value.diagnosisText)
    }

    @Test
    fun `startTask calls repository startTask and reloads data`() = runTest {
        val taskId = 101
        whenever(mechanicRepository.getOrderById(orderId)).thenReturn(Result.success(mock(MechanicOrder::class.java)))
        whenever(mechanicRepository.getTasksForOrder(orderId)).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.getInventoryItems()).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.startTask(taskId)).thenReturn(Result.success(Unit))

        val viewModel = createViewModel()
        viewModel.startTask(taskId)

        verify(mechanicRepository).startTask(taskId)
    }

    @Test
    fun `completeTask sends current diagnosisText to repository and reloads data`() = runTest {
        val taskId = 101
        val diagnosis = "Discos rectificados y pastillas calibradas"
        whenever(mechanicRepository.getOrderById(orderId)).thenReturn(Result.success(mock(MechanicOrder::class.java)))
        whenever(mechanicRepository.getTasksForOrder(orderId)).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.getInventoryItems()).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.completeTask(taskId, diagnosis)).thenReturn(Result.success(Unit))

        val viewModel = createViewModel()
        viewModel.updateDiagnosisText(diagnosis)
        viewModel.completeTask(taskId)

        verify(mechanicRepository).completeTask(taskId, diagnosis)
    }

    @Test
    fun `proposeTask calls repository proposeTask and reloads data`() = runTest {
        val input = MechanicProposeTaskInput(
            workOrderId = orderId,
            mechanicId = 5,
            description = "Cambio de manguera de freno cuarteada",
            priority = "URGENT",
            estimatedTime = 30,
            technicalDiagnosis = "Fuga potencial de líquido",
            parts = emptyList()
        )
        whenever(mechanicRepository.getOrderById(orderId)).thenReturn(Result.success(mock(MechanicOrder::class.java)))
        whenever(mechanicRepository.getTasksForOrder(orderId)).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.getInventoryItems()).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.proposeTask(input)).thenReturn(Result.success(Unit))

        val viewModel = createViewModel()
        viewModel.proposeTask(input)

        verify(mechanicRepository).proposeTask(input)
    }

    @Test
    fun `getCurrentMechanicId returns mechanicId from session`() = runTest {
        val session = UserSession(
            userId = 1,
            email = "mario@autoservice.com",
            role = "ROLE_MECHANIC",
            workshopId = "WS-01",
            mechanicId = 15,
            token = "jwt"
        )
        whenever(sessionManager.currentSession()).thenReturn(session)
        whenever(mechanicRepository.getOrderById(orderId)).thenReturn(Result.success(mock(MechanicOrder::class.java)))
        whenever(mechanicRepository.getTasksForOrder(orderId)).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.getInventoryItems()).thenReturn(Result.success(emptyList()))

        val viewModel = createViewModel()

        assertEquals(15, viewModel.getCurrentMechanicId())
    }

    @Test
    fun `failed start retains loaded order and shows actionable error without reporting success`() = runTest {
        val order = mock(MechanicOrder::class.java)
        whenever(mechanicRepository.getOrderById(orderId)).thenReturn(Result.success(order))
        whenever(mechanicRepository.getTasksForOrder(orderId)).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.getInventoryItems()).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.startTask(101)).thenReturn(Result.failure(IllegalStateException("Stock insuficiente")))
        val vm = createViewModel()
        vm.startTask(101)
        assertEquals("Stock insuficiente",vm.state.value.actionError)
        assertEquals(order,vm.state.value.order)
        assertEquals(0,vm.state.value.savedVersion)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test
    fun `invalid report never calls API and valid report increments saved version`() = runTest {
        whenever(mechanicRepository.getOrderById(orderId)).thenReturn(Result.success(mock(MechanicOrder::class.java)))
        whenever(mechanicRepository.getTasksForOrder(orderId)).thenReturn(Result.success(emptyList()))
        whenever(mechanicRepository.getInventoryItems()).thenReturn(Result.success(emptyList()))
        val report = com.torquelab.autoservice.mechanic.domain.model.TaskReport("Diagnóstico","Explicación","Privada","https://example.com/foto")
        whenever(mechanicRepository.saveTaskReport(101,report,true)).thenReturn(Result.success(Unit))
        val vm = createViewModel()
        vm.saveReport(101,report.copy(evidenceRegistered="javascript:alert(1)"),true)
        assertEquals(0,vm.state.value.savedVersion)
        org.mockito.kotlin.verify(mechanicRepository,org.mockito.kotlin.never()).saveTaskReport(101,report.copy(evidenceRegistered="javascript:alert(1)"),true)
        vm.saveReport(101,report,true)
        verify(mechanicRepository).saveTaskReport(101,report,true)
        assertEquals(1,vm.state.value.savedVersion)
        assertNull(vm.state.value.actionError)
    }
}
