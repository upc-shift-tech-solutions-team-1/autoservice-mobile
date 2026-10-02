package com.torquelab.autoservice.fleet.domain

data class Vehicle(
    val id: Int = 0,
    val plate: String = "",
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val color: String = "",
    val status: String = "IN_WORKSHOP",
    val image: String = "",
    val customerId: Int = 0,
    val engineType: String = "",
    val recommendedOil: String = "",
    val currentKilometers: Int = 0,
    val vin: String = ""
) {
    fun matches(query: String, owner: String = ""): Boolean =
        listOf(plate, brand, model, owner, engineType, recommendedOil, vin).any { it.contains(query.trim(), ignoreCase = true) }
}

data class VehicleOwner(val id: Int, val fullName: String = "")

enum class VehicleIssue { PLATE, BRAND_MODEL, YEAR, OWNER, STATUS, KILOMETERS }

fun Vehicle.validate(currentYear: Int): VehicleIssue? = when {
    plate.isBlank() || plate.trim().length > 15 -> VehicleIssue.PLATE
    brand.isBlank() || model.isBlank() -> VehicleIssue.BRAND_MODEL
    year.toIntOrNull()?.let { it in 1900..(currentYear + 1) } != true -> VehicleIssue.YEAR
    customerId <= 0 -> VehicleIssue.OWNER
    status !in listOf("IN_WORKSHOP", "READY", "DELIVERED") -> VehicleIssue.STATUS
    currentKilometers < 0 -> VehicleIssue.KILOMETERS
    else -> null
}

interface FleetRepository {
    suspend fun vehicles(): List<Vehicle>
    suspend fun owners(): List<VehicleOwner>
    suspend fun save(vehicle: Vehicle): Vehicle
    suspend fun updateKilometers(vehicleId: Int, newKilometers: Int): Vehicle {
        val existing = vehicles().find { it.id == vehicleId } ?: throw IllegalArgumentException("Vehículo no encontrado")
        return save(existing.copy(currentKilometers = newKilometers))
    }
}
