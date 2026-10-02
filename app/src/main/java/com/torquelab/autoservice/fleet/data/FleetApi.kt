package com.torquelab.autoservice.fleet.data

import com.torquelab.autoservice.fleet.domain.*
import retrofit2.http.*
import retrofit2.Retrofit
import javax.inject.Inject

// Explicit request contract: no id is sent when creating/updating a vehicle.
data class VehicleRequest(
    val plate: String,
    val brand: String,
    val model: String,
    val year: String,
    val color: String,
    val status: String,
    val image: String,
    val customerId: Int,
    val engineType: String = "",
    val recommendedOil: String = "",
    val currentKilometers: Int = 0,
    val vin: String = ""
)

data class UpdateKilometersRequest(val kilometers: Int)

interface FleetApi {
    @GET("vehicles") suspend fun vehicles(): List<Vehicle>
    @GET("customers") suspend fun owners(): List<VehicleOwner>
    @POST("vehicles") suspend fun create(@Body body: VehicleRequest): Vehicle
    @PUT("vehicles/{id}") suspend fun update(@Path("id") id: Int, @Body body: VehicleRequest): Vehicle
    @PATCH("vehicles/{id}/odometer") suspend fun updateKilometers(@Path("id") id: Int, @Body body: UpdateKilometersRequest): Vehicle
}

class RemoteFleetRepository @Inject constructor(retrofit: Retrofit) : FleetRepository {
    private val api = retrofit.create(FleetApi::class.java)
    override suspend fun vehicles() = api.vehicles()
    override suspend fun owners() = api.owners()
    override suspend fun save(vehicle: Vehicle): Vehicle {
        val body = VehicleRequest(
            plate = vehicle.plate.trim().uppercase(),
            brand = vehicle.brand.trim(),
            model = vehicle.model.trim(),
            year = vehicle.year.trim(),
            color = vehicle.color.trim(),
            status = vehicle.status,
            image = vehicle.image,
            customerId = vehicle.customerId,
            engineType = vehicle.engineType.trim(),
            recommendedOil = vehicle.recommendedOil.trim(),
            currentKilometers = vehicle.currentKilometers,
            vin = vehicle.vin.trim().uppercase()
        )
        return if (vehicle.id == 0) api.create(body) else api.update(vehicle.id, body)
    }

    override suspend fun updateKilometers(vehicleId: Int, newKilometers: Int): Vehicle {
        return api.updateKilometers(vehicleId, UpdateKilometersRequest(newKilometers))
    }
}
