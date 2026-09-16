package com.torquelab.autoservice.customer_trust.data.remote

import com.torquelab.autoservice.customer_trust.data.remote.dto.TrackingOrderDto
import retrofit2.http.GET
import retrofit2.http.Path

interface TrackingApi {
    @GET("tracking/{code}")
    suspend fun getOrderByCode(@Path("code") code: String): TrackingOrderDto
}
