package com.torquelab.autoservice.staff

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.torquelab.autoservice.R
import com.torquelab.autoservice.staff.domain.model.CreateMechanicInput
import com.torquelab.autoservice.staff.domain.model.Mechanic
import com.torquelab.autoservice.staff.presentation.StaffScreen
import com.torquelab.autoservice.staff.presentation.StaffUiState
import com.torquelab.autoservice.workshop.domain.model.FinancialPeriodPoint
import com.torquelab.autoservice.workshop.presentation.DashboardScreen
import com.torquelab.autoservice.workshop.presentation.DashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Uses the real forms, without creating users or writing to the backend. */
@RunWith(AndroidJUnit4::class)
class StaffAndDashboardUiTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)

    @Test fun registeringMechanicOffersWebSpecialtiesAndSubmitsSelectedValue() {
        var submitted: CreateMechanicInput? = null
        compose.setContent { MaterialTheme {
            StaffScreen(StaffUiState(isLoading = false), {}, { submitted = it })
        } }
        compose.onNodeWithText(text(R.string.staff_add_mechanic)).performClick()
        compose.onNodeWithText(text(R.string.staff_name)).performTextInput("Mecánico de prueba UI")
        compose.onNodeWithText(text(R.string.staff_email)).performTextInput("mechanic-ui@example.invalid")
        compose.onNodeWithText("${text(R.string.staff_specialty)}: Seleccione").performScrollTo().performClick()
        val specialties = listOf(R.string.staff_specialty_general, R.string.staff_specialty_electrical,
            R.string.staff_specialty_brakes, R.string.staff_specialty_tires)
        specialties.forEach { compose.onNodeWithText(text(it)).assertExists() }
        compose.onNodeWithText(text(R.string.staff_specialty_electrical)).performClick()
        compose.onNodeWithText(text(R.string.staff_password)).performScrollTo().performTextInput("ui-test-only")
        compose.onNodeWithText(text(R.string.common_save)).performClick()
        compose.runOnIdle {
            assertEquals(text(R.string.staff_specialty_electrical), submitted?.specialty)
            assertEquals(3, submitted?.maxCapacity)
        }
    }

    @Test fun editingPreservesLegacySpecialtyUntilUserChoosesAnother() {
        val original = Mechanic(7, "Mecánico UI", "mechanic-ui@example.invalid", "Especialidad anterior",
            3, 0, "TEST-UI", true)
        var submitted: Mechanic? = null
        compose.setContent { MaterialTheme {
            StaffScreen(StaffUiState(isLoading = false, mechanics = listOf(original)), {}, {}, { submitted = it })
        } }
        compose.onNodeWithText("Editar").performClick()
        compose.onNodeWithText("${text(R.string.staff_specialty)}: Especialidad anterior").performScrollTo().performClick()
        compose.onNode(hasText("Especialidad anterior") and hasClickAction()).assertExists()
        compose.onNodeWithText(text(R.string.staff_specialty_brakes)).performClick()
        compose.onNodeWithText("Guardar").performClick()
        compose.runOnIdle {
            assertEquals(original.copy(specialty = text(R.string.staff_specialty_brakes)), submitted)
        }
    }

    @Test fun dashboardKeepsMetricsButDoesNotRenderSevenDaySeries() {
        compose.setContent { MaterialTheme {
            DashboardScreen(DashboardUiState(isLoading = false, projectedIncome = 123.0,
                periodSeries = listOf(FinancialPeriodPoint("2026-11-07", "SERIE-NO-VISIBLE", 123.0, 23.0, 100.0))), {})
        } }
        compose.onNodeWithText("Rendimiento Financiero").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Evolución financiera (últimos 7 días)").assertDoesNotExist()
        compose.onNodeWithText("Acciones Rápidas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("SERIE-NO-VISIBLE", substring = true).assertDoesNotExist()
    }
}
