package com.torquelab.autoservice.staff

import com.torquelab.autoservice.staff.domain.model.Mechanic
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StaffDomainTest {

    @Test
    fun `mechanic is available below maximum capacity`() {
        val mechanic = Mechanic(
            id = 1,
            fullName = "Ana Rojas",
            email = "ana@example.com",
            specialty = "Diagnostics",
            maxCapacity = 3,
            assignedTasks = 2,
            workshopId = "WS-1",
            isAvailable = true
        )

        assertTrue(mechanic.isAvailable)
    }

    @Test
    fun `mechanic is unavailable at maximum capacity`() {
        val mechanic = Mechanic(
            id = 1,
            fullName = "Ana Rojas",
            email = "ana@example.com",
            specialty = "Diagnostics",
            maxCapacity = 3,
            assignedTasks = 3,
            workshopId = "WS-1",
            isAvailable = false
        )

        assertFalse(mechanic.isAvailable)
    }
}
