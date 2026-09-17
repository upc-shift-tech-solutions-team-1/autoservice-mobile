package com.torquelab.autoservice.staff.data.remote

import com.torquelab.autoservice.staff.data.remote.dto.CreateMechanicRequest
import com.torquelab.autoservice.staff.data.remote.dto.MechanicDto
import com.torquelab.autoservice.staff.data.remote.dto.UpdateMechanicRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StaffApi {

    @POST("mechanics")
    suspend fun createMechanic(@Body request: CreateMechanicRequest): MechanicDto

    @GET("mechanics")
    suspend fun getMechanics(): List<MechanicDto>

    @GET("mechanics/{id}")
    suspend fun getMechanic(@Path("id") id: Int): MechanicDto

    @PUT("mechanics/{id}")
    suspend fun updateMechanic(
        @Path("id") id: Int,
        @Body request: UpdateMechanicRequest
    ): MechanicDto

    @DELETE("mechanics/{id}")
    suspend fun deleteMechanic(@Path("id") id: Int)
}
