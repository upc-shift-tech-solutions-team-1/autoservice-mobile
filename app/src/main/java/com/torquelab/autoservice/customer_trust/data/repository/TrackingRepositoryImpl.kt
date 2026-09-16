package com.torquelab.autoservice.customer_trust.data.repository

import com.torquelab.autoservice.customer_trust.data.remote.TrackingApi
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder
import com.torquelab.autoservice.customer_trust.domain.repository.TrackingRepository
import javax.inject.Inject

class TrackingRepositoryImpl @Inject constructor(
    private val api: TrackingApi
) : TrackingRepository {
    override suspend fun getOrderByCode(code: String): Result<TrackingOrder> = runCatching {
        api.getOrderByCode(code).toDomain()
    }
}
