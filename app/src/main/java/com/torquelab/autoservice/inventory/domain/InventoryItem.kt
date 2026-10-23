package com.torquelab.autoservice.inventory.domain

import com.torquelab.autoservice.inventory.domain.model.Product
import com.torquelab.autoservice.inventory.domain.model.ProductStatus
import java.math.BigDecimal
import java.math.RoundingMode

data class InventoryItem(
    val id: Int = 0,
    val code: String = "",
    val name: String = "",
    val category: String = "SPARE_PART",
    val brand: String = "",
    val unitPrice: BigDecimal = BigDecimal.ZERO,
    val purchasePrice: BigDecimal = BigDecimal.ZERO,
    val minStock: Int = 3,
    val stock: Int = 0,
    val image: String = "",
    val qualityTier: String = "STANDARD",
    val specification: String = "",
    val presentation: String = "",
    val unitMeasure: String = "UNIT"
) {
    val currentStock: Int get() = stock
    val minimumStock: Int get() = minStock
    val lowStock: Boolean get() = currentStock <= minimumStock
    val profit: BigDecimal get() = unitPrice - purchasePrice
    val margin: BigDecimal get() = if (unitPrice.signum() == 0) BigDecimal.ZERO
        else profit.multiply(BigDecimal(100)).divide(unitPrice, 2, RoundingMode.HALF_UP)
    fun matches(query: String): Boolean = listOf(name, brand, category, specification, code)
        .any { it.contains(query.trim(), ignoreCase = true) }

    fun toProduct(): Product = Product(
        id = id,
        code = code,
        name = name,
        category = category,
        brand = brand,
        unitPrice = unitPrice,
        purchasePrice = purchasePrice,
        currentStock = stock,
        minimumStock = minStock,
        status = if (stock <= minStock) ProductStatus.LOW_STOCK else ProductStatus.NORMAL,
        image = image,
        qualityTier = qualityTier,
        specification = specification,
        presentation = presentation,
        unitMeasure = unitMeasure
    )
}

enum class InventoryIssue { NAME, PRICE, MIN_STOCK }

fun InventoryItem.validate(): InventoryIssue? = when {
    name.isBlank() -> InventoryIssue.NAME
    unitPrice.signum() < 0 || purchasePrice.signum() < 0 -> InventoryIssue.PRICE
    minStock < 0 -> InventoryIssue.MIN_STOCK
    else -> null
}

data class StockReceipt(val quantity: Int, val providerName: String, val documentNumber: String? = null, val notes: String? = null) {
    fun isValid(): Boolean = quantity > 0 && providerName.isNotBlank()
}

interface InventoryRepository {
    suspend fun items(): List<InventoryItem>
    suspend fun save(item: InventoryItem): InventoryItem
    suspend fun receive(id: Int, receipt: StockReceipt): InventoryItem
}
