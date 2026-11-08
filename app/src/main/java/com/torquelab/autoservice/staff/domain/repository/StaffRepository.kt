package com.torquelab.autoservice.staff.domain.repository

import com.torquelab.autoservice.staff.domain.model.CreateMechanicInput
import com.torquelab.autoservice.staff.domain.model.Mechanic

interface StaffRepository {

    suspend fun getMechanics(): Result<List<Mechanic>>

    suspend fun getMechanic(id: Int): Result<Mechanic>

    suspend fun registerMechanic(input: CreateMechanicInput): Result<Unit>
    suspend fun updateMechanic(mechanic: Mechanic): Result<Unit>
    suspend fun deleteMechanic(id: Int): Result<Unit>
}
