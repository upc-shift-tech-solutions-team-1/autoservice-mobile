package com.torquelab.autoservice.staff.data.repository

import com.torquelab.autoservice.staff.data.remote.StaffApi
import com.torquelab.autoservice.staff.data.remote.dto.CreateMechanicRequest
import com.torquelab.autoservice.staff.data.remote.dto.MechanicDto
import com.torquelab.autoservice.staff.domain.model.CreateMechanicInput
import com.torquelab.autoservice.staff.domain.model.Mechanic
import com.torquelab.autoservice.staff.domain.repository.StaffRepository
import com.torquelab.autoservice.workshop.data.remote.TaskApi
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepositoryImpl @Inject constructor(
    private val staffApi: StaffApi,
    private val taskApi: TaskApi
) : StaffRepository {

    override suspend fun getMechanics(): Result<List<Mechanic>> = runCatching {
        staffApi.getMechanics().map { mechanic ->
            mechanic.toDomain(
                assignedTasks = activeTaskCount(mechanic.id)
            )
        }
    }

    override suspend fun getMechanic(id: Int): Result<Mechanic> = runCatching {
        val mechanic = staffApi.getMechanic(id)
        mechanic.toDomain(activeTaskCount(mechanic.id))
    }

    override suspend fun registerMechanic(input: CreateMechanicInput): Result<Unit> = runCatching {
        staffApi.createMechanic(
            CreateMechanicRequest(
                fullName = input.fullName,
                specialty = input.specialty,
                maxCapacity = input.maxCapacity,
                email = input.email,
                password = input.password
            )
        )
        Unit
    }

    private suspend fun activeTaskCount(mechanicId: Int): Int = taskApi
        .getTasks(mechanicId = mechanicId)
        .count { task ->
            val status = TaskStatus.fromApi(task.status)
            status != TaskStatus.COMPLETED &&
                status != TaskStatus.DELIVERED &&
                status != TaskStatus.CANCELLED
        }

    private fun MechanicDto.toDomain(assignedTasks: Int) = Mechanic(
        id = id,
        fullName = fullName,
        email = email,
        specialty = specialty,
        maxCapacity = maxCapacity,
        assignedTasks = assignedTasks,
        workshopId = workshopId,
        isAvailable = assignedTasks < maxCapacity
    )
}
