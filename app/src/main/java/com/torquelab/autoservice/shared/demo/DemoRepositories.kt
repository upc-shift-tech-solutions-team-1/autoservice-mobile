package com.torquelab.autoservice.shared.demo

import com.torquelab.autoservice.fleet.domain.*
import com.torquelab.autoservice.inventory.domain.*
import java.math.BigDecimal

// Per-screen-session memory only. Never calls Retrofit or saves authentication tokens.
class DemoFleetRepository : FleetRepository {
    private val data = mutableListOf(
        Vehicle(1, "DEMO-01", "Toyota", "Corolla", "2021", "Silver", "IN_WORKSHOP", customerId = 1),
        Vehicle(2, "DEMO-02", "Kia", "Rio", "2020", "Blue", "READY", customerId = 2)
    )
    override suspend fun vehicles() = data.toList()
    override suspend fun owners() = listOf(VehicleOwner(1, "Cliente Demo A"), VehicleOwner(2, "Cliente Demo B"))
    override suspend fun save(vehicle: Vehicle): Vehicle {
        require(vehicle.customerId in listOf(1, 2))
        val saved = vehicle.copy(id = if (vehicle.id == 0) (data.maxOfOrNull { it.id } ?: 0) + 1 else vehicle.id,
            plate = vehicle.plate.trim().uppercase())
        val index = data.indexOfFirst { it.id == saved.id }
        if (index < 0) data.add(saved) else data[index] = saved
        return saved
    }
}

class DemoInventoryRepository : InventoryRepository {
    private val data = mutableListOf(
        InventoryItem(1, "Filtro de aceite", brand = "Bosch", unitPrice = BigDecimal("45.00"), purchasePrice = BigDecimal("30.00"), stock = 2),
        InventoryItem(2, "Aceite 5W-30", category = "LUBRICANT", brand = "Mobil", unitPrice = BigDecimal("120.00"), purchasePrice = BigDecimal("85.00"), stock = 12)
    )
    override suspend fun items() = data.toList()
    override suspend fun save(item: InventoryItem): InventoryItem {
        require(item.validate() == null)
        val index = data.indexOfFirst { it.id == item.id }
        val saved = item.copy(id = if (item.id == 0) (data.maxOfOrNull { it.id } ?: 0) + 1 else item.id,
            stock = if (index < 0) 0 else data[index].stock)
        if (index < 0) data.add(saved) else data[index] = saved
        return saved
    }
    override suspend fun receive(id: Int, receipt: StockReceipt): InventoryItem {
        require(receipt.isValid())
        val index = data.indexOfFirst { it.id == id }
        require(index >= 0)
        val item = data[index]
        require(receipt.quantity <= Int.MAX_VALUE - item.stock)
        return item.copy(stock = item.stock + receipt.quantity).also { data[index] = it }
    }
}
