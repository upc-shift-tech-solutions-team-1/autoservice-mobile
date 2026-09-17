package com.torquelab.autoservice.management

import com.torquelab.autoservice.fleet.domain.*
import com.torquelab.autoservice.inventory.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class ManagementDomainTest {
    private val vehicle = Vehicle(plate = "ABC-123", brand = "Toyota", model = "Corolla", year = "2021", customerId = 1)

    @Test fun validVehiclePasses() { assertNull(vehicle.validate(2026)) }
    @Test fun blankPlateRejected() { assertEquals(VehicleIssue.PLATE, vehicle.copy(plate = "  ").validate(2026)) }
    @Test fun missingModelRejected() { assertEquals(VehicleIssue.BRAND_MODEL, vehicle.copy(model = "").validate(2026)) }
    @Test fun invalidYearRejected() {
        listOf("abc", "1899", "2028").forEach { assertEquals(VehicleIssue.YEAR, vehicle.copy(year = it).validate(2026)) }
    }
    @Test fun nextModelYearAccepted() { assertNull(vehicle.copy(year = "2027").validate(2026)) }
    @Test fun ownerRequired() { assertEquals(VehicleIssue.OWNER, vehicle.copy(customerId = 0).validate(2026)) }
    @Test fun unknownStatusRejected() { assertEquals(VehicleIssue.STATUS, vehicle.copy(status = "OTHER").validate(2026)) }
    @Test fun searchMatchesPlateAndOwnerIgnoringCaseAndSpaces() {
        assertTrue(vehicle.matches(" abc "))
        assertTrue(vehicle.matches("mario", "Mario Demo"))
        assertFalse(vehicle.matches("XYZ"))
    }
    @Test fun lowStockIncludesThreshold() {
        assertTrue(InventoryItem(stock = 3, minStock = 3).lowStock)
        assertFalse(InventoryItem(stock = 4, minStock = 3).lowStock)
    }
    @Test fun marginUsesSalePriceAndDecimalRounding() {
        val item = InventoryItem(unitPrice = BigDecimal("45"), purchasePrice = BigDecimal("30"))
        assertEquals(BigDecimal("15"), item.profit)
        assertEquals(BigDecimal("33.33"), item.margin)
    }
    @Test fun zeroSalePriceDoesNotDivideByZero() { assertEquals(BigDecimal.ZERO, InventoryItem().margin) }
    @Test fun lossesAreDisplayedAsNegativeMargins() {
        assertEquals(BigDecimal("-25.00"), InventoryItem(unitPrice = BigDecimal("40"), purchasePrice = BigDecimal("50")).margin)
    }
    @Test fun invalidProductRejected() {
        assertEquals(InventoryIssue.NAME, InventoryItem(name = " ").validate())
        assertEquals(InventoryIssue.PRICE, InventoryItem(name = "Oil", purchasePrice = BigDecimal("-1")).validate())
        assertEquals(InventoryIssue.MIN_STOCK, InventoryItem(name = "Oil", minStock = -1).validate())
    }
    @Test fun receiptRequiresProviderAndPositiveQuantity() {
        assertFalse(StockReceipt(0, "Provider").isValid())
        assertFalse(StockReceipt(-1, "Provider").isValid())
        assertFalse(StockReceipt(3, " ").isValid())
        assertTrue(StockReceipt(3, "Provider").isValid())
    }
}
