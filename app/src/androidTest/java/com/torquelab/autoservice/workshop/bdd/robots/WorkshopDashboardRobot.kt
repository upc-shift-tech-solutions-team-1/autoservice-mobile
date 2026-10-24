package com.torquelab.autoservice.workshop.bdd.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput

/**
 * Robot Pattern for interacting with the Workshop Dashboard and Operations UI in Compose.
 */
class WorkshopDashboardRobot(private val composeTestRule: ComposeContentTestRule) {

    fun assertActiveVehiclesCount(count: Int) {
        composeTestRule.onNodeWithText("$count", substring = true)
            .assertIsDisplayed()
    }

    fun assertFinancialMetricVisible(metricLabel: String) {
        composeTestRule.onNodeWithText(metricLabel, substring = true)
            .assertIsDisplayed()
    }

    fun clickCreateTaskButton() {
        composeTestRule.onNodeWithText("Nueva Tarea", ignoreCase = true)
            .performClick()
    }

    fun enterTaskDescription(description: String) {
        composeTestRule.onNodeWithText("Descripción", substring = true)
            .performTextInput(description)
    }

    fun enterLaborPrice(price: String) {
        composeTestRule.onNodeWithText("Precio Mano de Obra", substring = true)
            .performTextInput(price)
    }

    fun clickSaveTask() {
        composeTestRule.onNodeWithText("Guardar", ignoreCase = true)
            .performClick()
    }
}
