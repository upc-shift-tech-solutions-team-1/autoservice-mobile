package com.torquelab.autoservice.workshop.data.remote

import com.torquelab.autoservice.workshop.data.remote.dto.CreateTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.PatchTaskRequest
import com.torquelab.autoservice.workshop.data.remote.dto.TaskDto
import com.torquelab.autoservice.workshop.data.remote.dto.UpdateTaskRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApi {

    @POST("tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): TaskDto

    @GET("tasks")
    suspend fun getTasks(
        @Query("workOrderId") workOrderId: Int? = null,
        @Query("mechanicId") mechanicId: Int? = null
    ): List<TaskDto>

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body request: UpdateTaskRequest
    ): TaskDto

    @PATCH("tasks/{id}")
    suspend fun patchTask(
        @Path("id") id: Int,
        @Body request: PatchTaskRequest
    ): TaskDto

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int)
}
