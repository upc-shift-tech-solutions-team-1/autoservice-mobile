package com.torquelab.autoservice.workshop.data.remote.dto

import com.torquelab.autoservice.workshop.domain.model.FinancialSummary

data class FinancialSummaryDto(
    val projectedRevenue: Double?,
    val realizedRevenue: Double?,
    val pendingRevenue: Double?,
    val operatingCost: Double?,
    val grossProfit: Double?,
    val marginPercentage: Double?,
    val profitableOrders: Int?,
    val lossOrders: Int?,
    val averageTicket: Double?,
    val periodSeries: List<com.torquelab.autoservice.workshop.domain.model.FinancialPeriodPoint>? = null,
    val orderProfitability: List<com.torquelab.autoservice.workshop.domain.model.OrderProfitability>? = null
) {
    fun toDomain(): FinancialSummary {
        return FinancialSummary(
            projectedRevenue = projectedRevenue ?: 0.0,
            realizedRevenue = realizedRevenue ?: 0.0,
            pendingRevenue = pendingRevenue ?: 0.0,
            operatingCost = operatingCost ?: 0.0,
            grossProfit = grossProfit ?: 0.0,
            marginPercentage = marginPercentage ?: 0.0,
            profitableOrders = profitableOrders ?: 0,
            lossOrders = lossOrders ?: 0,
            averageTicket = averageTicket ?: 0.0,
            periodSeries = periodSeries.orEmpty(),
            orderProfitability = orderProfitability.orEmpty()
        )
    }
}
