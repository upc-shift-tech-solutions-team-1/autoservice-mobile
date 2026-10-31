
package com.torquelab.autoservice.inventory.bdd

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.torquelab.autoservice.inventory.bdd.steps.InventoryStockSteps
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InventoryStockBddTest {

    private lateinit var steps: InventoryStockSteps

    @Before
    fun setUp() {
        steps = InventoryStockSteps()
    }

    @Test
    fun scenario_receiveMaterialsIncreasesStock() = runBlocking {
        // Given
        steps.givenProductWithStock(5)
        steps.givenRequestedQuantity(5)

        // When
        steps.whenReceivingMaterials()

        // Then
        steps.thenStockShouldBe(10)
        steps.thenProductShouldBeSaved()
    }

    @Test
    fun scenario_rejectConsumptionWhenStockIsInsufficient() = runBlocking {
        // Given
        steps.givenProductWithStock(5)
        steps.givenRequestedQuantity(10)

        // When
        steps.whenConsumingMaterials()

        // Then
        steps.thenOperationShouldBeRejectedForInsufficientStock()
        steps.thenNoChangesShouldBeSaved()
    }
}
