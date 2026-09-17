package com.torquelab.autoservice.management

import com.google.gson.Gson
import com.torquelab.autoservice.fleet.domain.*
import com.torquelab.autoservice.inventory.domain.*
import com.torquelab.autoservice.inventory.data.toRequest
import com.torquelab.autoservice.shared.demo.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class ManagementRepositoryTest {
    @Test fun catalogRequestCannotOverwriteStock() {
        val json = Gson().toJsonTree(InventoryItem(name = "Oil", stock = 999, unitPrice = BigDecimal("12.50")).toRequest()).asJsonObject
        assertFalse(json.has("stock"))
        assertFalse(json.has("id"))
        assertEquals(BigDecimal("12.50"), json.get("unitPrice").asBigDecimal)
    }
    @Test fun newCatalogItemStartsAtZero() = runBlocking {
        val repo = DemoInventoryRepository()
        assertEquals(0, repo.save(InventoryItem(name = "Oil", stock = 999)).stock)
    }
    @Test fun editingProductPreservesStock() = runBlocking {
        val repo = DemoInventoryRepository()
        val original = repo.items().first()
        val saved = repo.save(original.copy(name = "Updated", stock = 999))
        assertEquals(original.stock, saved.stock)
        assertEquals("Updated", saved.name)
    }
    @Test fun receiptAddsQuantityAndPersistsWithinDemoSession() = runBlocking {
        val repo = DemoInventoryRepository()
        val original = repo.items().first()
        val updated = repo.receive(original.id, StockReceipt(5, "Provider"))
        assertEquals(original.stock + 5, updated.stock)
        assertEquals(updated, repo.items().first())
    }
    @Test fun invalidReceiptDoesNotMutateStock() = runBlocking {
        val repo = DemoInventoryRepository()
        val before = repo.items()
        try { repo.receive(before.first().id, StockReceipt(-5, "Provider")); fail("Expected rejection") }
        catch (_: IllegalArgumentException) { }
        assertEquals(before, repo.items())
    }
    @Test fun demoSessionsAreIndependent() = runBlocking {
        val first = DemoInventoryRepository()
        first.receive(1, StockReceipt(4, "Provider"))
        assertEquals(2, DemoInventoryRepository().items().first().stock)
    }
    @Test fun updatingVehicleDoesNotDuplicateIt() = runBlocking {
        val repo = DemoFleetRepository()
        val original = repo.vehicles().first()
        repo.save(original.copy(plate = " new-01 "))
        assertEquals(2, repo.vehicles().size)
        assertEquals("NEW-01", repo.vehicles().first().plate)
    }
    @Test fun newVehiclesHaveUniqueIds() = runBlocking {
        val repo = DemoFleetRepository()
        val first = repo.save(Vehicle(plate = "ABC-001", customerId = 1))
        val second = repo.save(Vehicle(plate = "ABC-002", customerId = 2))
        assertNotEquals(first.id, second.id)
        assertEquals(4, repo.vehicles().size)
    }
}
