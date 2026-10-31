package com.torquelab.autoservice.mechanic.bdd.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput

/**
 * Robot Pattern for interacting with the Mechanic Order Execution UI in Compose.
 */
class MechanicOrderRobot(private val composeTestRule: ComposeContentTestRule) {

    fun assertOrderHeaderVisible(vehiclePlate: String) {
        composeTestRule.onNodeWithText(vehiclePlate, substring = true)
            .assertIsDisplayed()
    }

    fun assertTaskDescriptionVisible(description: String) {
        composeTestRule.onNodeWithText(description, substring = true)
            .assertIsDisplayed()
    }

    fun clickStartTask() {
        composeTestRule.onNodeWithText("Iniciar", ignoreCase = true)
            .performClick()
    }

    fun enterDiagnosis(diagnosis: String) {
        composeTestRule.onNodeWithText("Diagnóstico Técnico", substring = true)
            .performTextInput(diagnosis)
    }

    fun clickCompleteTask() {
        composeTestRule.onNodeWithText("Completar Tarea", ignoreCase = true)
            .performClick()
    }

    fun assertProgressVisible(percentage: Int) {
        composeTestRule.onNodeWithText("$percentage%", substring = true)
            .assertIsDisplayed()
    }
}
