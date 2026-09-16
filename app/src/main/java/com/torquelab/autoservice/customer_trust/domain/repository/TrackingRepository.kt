package com.torquelab.autoservice.customer_trust.domain.repository

import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder

interface TrackingRepository {
    suspend fun getOrderByCode(code: String): Result<TrackingOrder>
}
