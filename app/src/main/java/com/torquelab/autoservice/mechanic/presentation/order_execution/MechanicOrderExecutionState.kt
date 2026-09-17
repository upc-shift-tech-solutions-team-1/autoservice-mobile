package com.torquelab.autoservice.mechanic.presentation.order_execution

import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.mechanic.domain.model.MechanicTask

data class MechanicOrderExecutionState(
    val order: MechanicOrder? = null,
    val tasks: List<MechanicTask> = emptyList(),
    val inventoryItems: List<InventoryItem> = emptyList(),
    val diagnosisText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
