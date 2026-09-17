package com.torquelab.autoservice.mechanic.presentation.workspace

import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder

data class MechanicDashboardState(
    val mechanicName: String = "",
    val mechanicSpecialty: String = "Technician",
    val orders: List<MechanicOrder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
