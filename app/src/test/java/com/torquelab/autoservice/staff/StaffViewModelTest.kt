
package com.torquelab.autoservice.staff

import com.torquelab.autoservice.staff.domain.model.CreateMechanicInput
import com.torquelab.autoservice.staff.domain.model.Mechanic
import com.torquelab.autoservice.staff.domain.repository.StaffRepository
import com.torquelab.autoservice.staff.presentation.StaffViewModel
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
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class StaffViewModelTest {

    private lateinit var repository: StaffRepository
    private lateinit var viewModel: StaffViewModel

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        repository = mock(StaffRepository::class.java)
        viewModel = StaffViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createMechanic() = Mechanic(
        id = 1,
        fullName = "Ana Rojas",
        email = "ana@example.com",
        specialty = "Diagnostico",
        maxCapacity = 3,
        assignedTasks = 2,
        workshopId = "WS-1",
        isAvailable = true
    )

    private fun createInput() = CreateMechanicInput(
        fullName = "Ana Rojas",
        email = "ana@example.com",
        specialty = "Diagnostico",
        maxCapacity = 3,
        password = "Password123"
    )

    @Test
    fun `load mechanics should update staff list successfully`() = runTest {
        // Arrange
        val mechanics = listOf(createMechanic())

        whenever(repository.getMechanics())
            .thenReturn(Result.success(mechanics))

        // Act
        viewModel.load()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(1, state.mechanics.size)
        assertEquals("Ana Rojas", state.mechanics.first().fullName)
        assertTrue(state.mechanics.first().isAvailable)
        assertNull(state.errorMessage)

        verify(repository).getMechanics()
    }

    @Test
    fun `load mechanics failure should show error message`() = runTest {
        // Arrange
        whenever(repository.getMechanics())
            .thenReturn(
                Result.failure(
                    Exception("No se pudo conectar al servidor")
                )
            )

        // Act
        viewModel.load()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.mechanics.isEmpty())
        assertEquals(
            "No se pudo conectar al servidor",
            state.errorMessage
        )

        verify(repository).getMechanics()
    }

    @Test
    fun `register mechanic successfully should refresh staff list`() = runTest {
        // Arrange
        val input = createInput()
        val mechanic = createMechanic()

        whenever(repository.registerMechanic(input))
            .thenReturn(Result.success(Unit))

        whenever(repository.getMechanics())
            .thenReturn(Result.success(listOf(mechanic)))

        // Act
        viewModel.registerMechanic(input)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertFalse(state.isSubmitting)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(1, state.mechanics.size)
        assertEquals("Ana Rojas", state.mechanics.first().fullName)

        verify(repository).registerMechanic(input)
        verify(repository).getMechanics()
    }

    @Test
    fun `register mechanic failure should show error and stop submitting`() = runTest {
        // Arrange
        val input = createInput()

        whenever(repository.registerMechanic(input))
            .thenReturn(
                Result.failure(
                    Exception("El correo ya esta registrado")
                )
            )

        // Act
        viewModel.registerMechanic(input)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertFalse(state.isSubmitting)
        assertEquals(
            "El correo ya esta registrado",
            state.errorMessage
        )
        assertTrue(state.mechanics.isEmpty())

        verify(repository).registerMechanic(input)
    }
}
