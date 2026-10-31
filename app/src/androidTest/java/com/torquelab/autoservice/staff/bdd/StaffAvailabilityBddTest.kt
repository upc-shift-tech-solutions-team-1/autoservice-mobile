
package com.torquelab.autoservice.staff.bdd

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.torquelab.autoservice.staff.bdd.steps.StaffAvailabilitySteps
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StaffAvailabilityBddTest {

    private lateinit var steps: StaffAvailabilitySteps

    @Before
    fun setUp() {
        steps = StaffAvailabilitySteps()
    }

    @Test
    fun scenario_mechanicHasAvailableCapacity() = runBlocking {
        // Given
        steps.givenMechanicCapacity(3)
        steps.givenActiveTasks(2)

        // When
        steps.whenAdministratorChecksAvailability()

        // Then
        steps.thenMechanicIsAvailable()
        steps.thenAssignedTasksAre(2)
    }

    @Test
    fun scenario_mechanicHasReachedMaximumCapacity() = runBlocking {
        // Given
        steps.givenMechanicCapacity(3)
        steps.givenActiveTasks(3)

        // When
        steps.whenAdministratorChecksAvailability()

        // Then
        steps.thenMechanicIsNotAvailable()
        steps.thenAssignedTasksAre(3)
    }
}
