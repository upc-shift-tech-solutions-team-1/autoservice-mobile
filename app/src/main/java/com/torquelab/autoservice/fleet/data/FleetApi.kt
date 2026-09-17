package com.torquelab.autoservice.fleet.data

import com.torquelab.autoservice.fleet.domain.*
import retrofit2.http.*
import retrofit2.Retrofit
import javax.inject.Inject

// Explicit request contract: no id is sent when creating/updating a vehicle.
data class VehicleRequest(val plate: String, val brand: String, val model: String,
    val year: String, val color: String, val status: String, val image: String, val customerId: Int)

interface FleetApi {
    @GET("vehicles") suspend fun vehicles(): List<Vehicle>
    @GET("customers") suspend fun owners(): List<VehicleOwner>
    @POST("vehicles") suspend fun create(@Body body: VehicleRequest): Vehicle
    @PUT("vehicles/{id}") suspend fun update(@Path("id") id: Int, @Body body: VehicleRequest): Vehicle
}

class RemoteFleetRepository @Inject constructor(retrofit: Retrofit) : FleetRepository {
    private val api = retrofit.create(FleetApi::class.java)
    override suspend fun vehicles() = api.vehicles()
    override suspend fun owners() = api.owners()
    override suspend fun save(vehicle: Vehicle): Vehicle {
        val body = VehicleRequest(vehicle.plate.trim().uppercase(), vehicle.brand.trim(),
            vehicle.model.trim(), vehicle.year.trim(), vehicle.color.trim(), vehicle.status,
            vehicle.image, vehicle.customerId)
        return if (vehicle.id == 0) api.create(body) else api.update(vehicle.id, body)
    }
}
