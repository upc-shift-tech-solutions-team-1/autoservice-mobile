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
    val averageTicket: Double,
    val periodSeries: List<FinancialPeriodPoint> = emptyList(),
    val orderProfitability: List<OrderProfitability> = emptyList()
)

data class FinancialPeriodPoint(val date: String, val label: String, val revenue: Double, val cost: Double, val profit: Double)
data class OrderProfitability(val workOrderId: Int, val trackingCode: String, val revenue: Double, val cost: Double, val profit: Double, val marginPercentage: Double)
