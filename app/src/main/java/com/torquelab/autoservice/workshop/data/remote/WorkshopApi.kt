package com.torquelab.autoservice.workshop.data.remote

import com.torquelab.autoservice.workshop.data.remote.dto.WorkOrderDto
import retrofit2.http.GET
import retrofit2.http.Path

interface WorkshopApi {

    @GET("workorders")
    suspend fun getWorkOrders(): List<WorkOrderDto>

    @GET("workorders/{id}")
    suspend fun getWorkOrder(@Path("id") id: Int): WorkOrderDto
}
