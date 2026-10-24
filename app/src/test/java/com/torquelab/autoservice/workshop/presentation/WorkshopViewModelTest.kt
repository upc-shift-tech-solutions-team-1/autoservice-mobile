package com.torquelab.autoservice.workshop.presentation

import com.torquelab.autoservice.staff.domain.model.Mechanic
import com.torquelab.autoservice.staff.domain.repository.StaffRepository
import com.torquelab.autoservice.workshop.domain.model.CreateTaskInput
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.UpdateTaskInput
import com.torquelab.autoservice.workshop.domain.model.WorkshopTask
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WorkshopViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: WorkshopRepository
    private lateinit var staffRepository: StaffRepository
    private lateinit var viewModel: WorkshopViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(WorkshopRepository::class.java)
        staffRepository = mock(StaffRepository::class.java)
        viewModel = WorkshopViewModel(repository, staffRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load attaches mechanic names to tasks and populates work orders`() = runTest {
        val mechanics = listOf(
            Mechanic(
                id = 10,
                fullName = "Roberto Gomez",
                email = "roberto@taller.com",
                specialty = "Frenos",
                maxCapacity = 3,
                assignedTasks = 1,
                workshopId = "WS-01",
                isAvailable = true
            )
        )

        val taskWithoutName = WorkshopTask(
            id = 1,
            orderId = 100,
            description = "Cambio de pastillas",
            priority = "HIGH",
            estimatedMinutes = 45,
            laborPrice = 70.0,
            materialsCost = 0.0,
            status = TaskStatus.IN_PROGRESS,
            mechanicId = 10,
            mechanicName = null
        )

        val orders = listOf(
            WorkshopWorkOrder(
                id = 100,
                vehicleId = 1,
                customerId = 1,
                trackingCode = "TRK-100",
                description = "Mantenimiento",
                status = TaskStatus.IN_PROGRESS,
                tasks = listOf(taskWithoutName)
            )
        )

        whenever(repository.getWorkOrders()).thenReturn(Result.success(orders))
        whenever(staffRepository.getMechanics()).thenReturn(Result.success(mechanics))

        viewModel.load()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(1, state.workOrders.size)
        assertEquals(mechanics, state.mechanics)

        // Verify mechanic name was mapped
        val mappedTask = state.workOrders[0].tasks[0]
        assertEquals("Roberto Gomez", mappedTask.mechanicName)
    }

    @Test
    fun `load sets errorMessage when repository fails`() = runTest {
        whenever(repository.getWorkOrders()).thenReturn(Result.failure(RuntimeException("Error de conexión")))
        whenever(staffRepository.getMechanics()).thenReturn(Result.success(emptyList()))

        viewModel.load()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Error de conexión", state.errorMessage)
    }

    @Test
    fun `createTask triggers repository createTask and reloads on success`() = runTest {
        whenever(repository.createTask(any())).thenReturn(Result.success(Unit))
        whenever(repository.getWorkOrders()).thenReturn(Result.success(emptyList()))
        whenever(staffRepository.getMechanics()).thenReturn(Result.success(emptyList()))

        viewModel.createTask(
            orderId = 100,
            description = "Nueva tarea",
            priority = "MEDIUM",
            estimatedMinutes = 30,
            laborPrice = 50.0,
            mechanicId = 10
        )

        verify(repository).createTask(
            CreateTaskInput(
                orderId = 100,
                description = "Nueva tarea",
                priority = "MEDIUM",
                estimatedMinutes = 30,
                laborPrice = 50.0,
                mechanicId = 10
            )
        )
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `cancelTask triggers repository cancelTask`() = runTest {
        whenever(repository.cancelTask(55)).thenReturn(Result.success(Unit))
        whenever(repository.getWorkOrders()).thenReturn(Result.success(emptyList()))
        whenever(staffRepository.getMechanics()).thenReturn(Result.success(emptyList()))

        viewModel.cancelTask(55)

        verify(repository).cancelTask(55)
    }

    @Test
    fun `updateTaskStatus triggers repository updateTaskStatus`() = runTest {
        whenever(repository.updateTaskStatus(55, TaskStatus.COMPLETED)).thenReturn(Result.success(Unit))
        whenever(repository.getWorkOrders()).thenReturn(Result.success(emptyList()))
        whenever(staffRepository.getMechanics()).thenReturn(Result.success(emptyList()))

        viewModel.updateTaskStatus(55, TaskStatus.COMPLETED)

        verify(repository).updateTaskStatus(55, TaskStatus.COMPLETED)
    }

    @Test
    fun `clearError resets errorMessage to null`() = runTest {
        whenever(repository.getWorkOrders()).thenReturn(Result.failure(RuntimeException("Fallo")))
        whenever(staffRepository.getMechanics()).thenReturn(Result.success(emptyList()))

        viewModel.load()
        assertEquals("Fallo", viewModel.uiState.value.errorMessage)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
