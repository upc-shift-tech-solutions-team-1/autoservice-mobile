package com.torquelab.autoservice.customer_trust.data.remote

import com.torquelab.autoservice.customer_trust.data.remote.dto.TrackingOrderDto
import com.torquelab.autoservice.customer_trust.data.remote.dto.TrackingCustomerDto
import com.torquelab.autoservice.customer_trust.data.remote.dto.TrackingTaskDto
import com.torquelab.autoservice.customer_trust.data.remote.dto.TrackingVehicleDto
import com.torquelab.autoservice.customer_trust.data.remote.dto.TrackingWorkshopDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TrackingApi {
    @GET("tracking/workorders")
    suspend fun getOrderByCode(@Query("trackingCode") trackingCode: String): List<TrackingOrderDto>

    @GET("tracking/vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: Int): TrackingVehicleDto

    @GET("tracking/tasks")
    suspend fun getTasksByOrder(@Query("workOrderId") workOrderId: Int): List<TrackingTaskDto>

    @GET("tracking/customers/{id}")
    suspend fun getCustomer(@Path("id") id: Int): TrackingCustomerDto

    @GET("tracking/workshops/{id}")
    suspend fun getWorkshop(@Path("id") id: String): TrackingWorkshopDto
}
