package com.torquelab.autoservice.fleet

import com.torquelab.autoservice.fleet.domain.Vehicle
import com.torquelab.autoservice.fleet.domain.usecase.RegisterVehicleUseCase
import com.torquelab.autoservice.fleet.presentation.VehicleUiState
import com.torquelab.autoservice.fleet.presentation.VehicleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var registerVehicleUseCase: RegisterVehicleUseCase
    private lateinit var viewModel: VehicleViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        registerVehicleUseCase = mock(RegisterVehicleUseCase::class.java)
        viewModel = VehicleViewModel(registerVehicleUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `registerVehicle emits Success when license plate and data are valid`() = runTest {
        val validVehicle = Vehicle(
            id = 1,
            plate = "ABC-123",
            brand = "Toyota",
            model = "Corolla",
            year = "2022",
            color = "White",
            customerId = 1,
            engineType = "2.0L 4-Cyl",
            recommendedOil = "5W-30",
            currentKilometers = 15000,
            vin = "1HGCR2F83HA000000"
        )

        whenever(registerVehicleUseCase.invoke(validVehicle)).thenReturn(Result.success(validVehicle))

        viewModel.registerVehicle(validVehicle)

        val state = viewModel.uiState.value
        assertTrue(state is VehicleUiState.Success)
        assertEquals(validVehicle, (state as VehicleUiState.Success).vehicle)
    }

    @Test
    fun `registerVehicle emits Error when vehicle data is invalid`() = runTest {
        val invalidVehicle = Vehicle(plate = "", brand = "", customerId = 0)
        val errorMessage = "Vehículo inválido: PLATE"

        whenever(registerVehicleUseCase.invoke(invalidVehicle))
            .thenReturn(Result.failure(IllegalArgumentException(errorMessage)))

        viewModel.registerVehicle(invalidVehicle)

        val state = viewModel.uiState.value
        assertTrue(state is VehicleUiState.Error)
        assertEquals(errorMessage, (state as VehicleUiState.Error).message)
    }
}
