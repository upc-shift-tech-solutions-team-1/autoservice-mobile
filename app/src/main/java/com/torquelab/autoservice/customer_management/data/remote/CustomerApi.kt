package com.torquelab.autoservice.customer_management.data.remote

import com.torquelab.autoservice.customer_management.data.remote.dto.CustomerDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CustomerApi {
    @GET("customers")
    suspend fun getCustomers(): List<CustomerDto>

    @GET("customers/{id}")
    suspend fun getCustomer(@Path("id") id: String): CustomerDto

    @POST("customers")
    suspend fun createCustomer(@Body customer: CustomerDto): CustomerDto

    @PUT("customers/{id}")
    suspend fun updateCustomer(@Path("id") id: String, @Body customer: CustomerDto): CustomerDto

    @DELETE("customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: String)
}
