
package com.torquelab.autoservice.customer_trust.bdd

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.torquelab.autoservice.customer_trust.bdd.steps.VehicleTrackingSteps
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VehicleTrackingBddTest {

    private lateinit var steps: VehicleTrackingSteps

    @Before
    fun setUp() {
        steps = VehicleTrackingSteps()
    }

    @Test
    fun scenario_customerTracksExistingOrder() = runBlocking {
        // Given
        steps.givenExistingOrder("TRK-100")
        steps.givenCustomerEntersCode("TRK-100")

        // When
        steps.whenCustomerSearchesTracking()

        // Then
        steps.thenOrderIsDisplayed("TRK-100")
        steps.thenVehiclePlateIsDisplayed("ABC-123")
        steps.thenNoErrorIsDisplayed()
    }

    @Test
    fun scenario_customerSearchesUnknownCode() = runBlocking {
        // Given
        steps.givenMissingOrder("TRK-404")
        steps.givenCustomerEntersCode("TRK-404")

        // When
        steps.whenCustomerSearchesTracking()

        // Then
        steps.thenOrderNotFoundErrorIsDisplayed()
        steps.thenNoOrderIsDisplayed()
    }
}
