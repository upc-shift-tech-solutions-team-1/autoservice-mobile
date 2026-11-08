package com.torquelab.autoservice.workshop.domain.model

import com.torquelab.autoservice.fleet.domain.Vehicle
import com.torquelab.autoservice.fleet.domain.VehicleOwner
import com.torquelab.autoservice.inventory.domain.InventoryItem
import java.time.LocalDate

data class TaskMaterialInput(val inventoryItemId: Int, val quantity: Int)
data class WorkshopOptions(
    val vehicles: List<Vehicle> = emptyList(),
    val customers: List<VehicleOwner> = emptyList(),
    val inventory: List<InventoryItem> = emptyList()
)
data class CreateWorkOrderInput(
    val vehicleId: Int, val customerId: Int, val mechanicId: Int,
    val description: String, val estimatedDate: String
)
data class QualityChecklist(
    val tasksCompleted: Boolean = false, val sparePartsChecked: Boolean = false,
    val diagnosisValidated: Boolean = false, val cleaningDone: Boolean = false,
    val finalTestDone: Boolean = false
) {
    val complete get() = tasksCompleted && sparePartsChecked && diagnosisValidated && cleaningDone && finalTestDone
}
val WorkshopWorkOrder.closed get() = status in setOf(TaskStatus.FINISHED, TaskStatus.DELIVERED, TaskStatus.CANCELLED)
fun WorkshopWorkOrder.canFinish(checks: QualityChecklist = checklist): Boolean =
    !closed && tasks.isNotEmpty() && tasks.all { it.status == TaskStatus.COMPLETED && it.adminReviewStatus == "APPROVED" } && checks.complete

/** Same date-only rule as the web. The API remains responsible for authorization and persistence. */
fun validateEstimatedDate(previous: String?, proposed: String, reason: String?): String? {
    val date = runCatching { LocalDate.parse(proposed) }.getOrNull()
        ?: return "Ingrese una fecha válida (AAAA-MM-DD)."
    val before = previous?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }
    return if (before != null && date.isAfter(before) && reason.isNullOrBlank())
        "Indique el motivo de la demora." else null
}
