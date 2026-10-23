package com.torquelab.autoservice.workshop.domain.model

data class FinancialSummary(
    val projectedRevenue: Double,
    val realizedRevenue: Double,
    val pendingRevenue: Double,
    val operatingCost: Double,
    val grossProfit: Double,
    val marginPercentage: Double,
    val profitableOrders: Int,
    val lossOrders: Int,
    val averageTicket: Double
)
