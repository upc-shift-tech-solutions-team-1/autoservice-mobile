package com.torquelab.autoservice.workshop.data.remote

import com.torquelab.autoservice.workshop.data.remote.dto.WorkOrderDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Body

interface WorkshopApi {
    @POST("workorders")
    suspend fun createWorkOrder(@Body body: com.torquelab.autoservice.workshop.data.remote.dto.CreateWorkOrderRequest): WorkOrderDto

    @PUT("workorders/{id}")
    suspend fun updateWorkOrder(@Path("id") id: Int, @Body body: com.torquelab.autoservice.workshop.data.remote.dto.UpdateWorkOrderRequest): WorkOrderDto

    @GET("workorders")
    suspend fun getWorkOrders(): List<WorkOrderDto>

    @GET("workorders/{id}")
    suspend fun getWorkOrder(@Path("id") id: Int): WorkOrderDto

    @GET("financial-summary")
    suspend fun getFinancialSummary(): com.torquelab.autoservice.workshop.data.remote.dto.FinancialSummaryDto
}
