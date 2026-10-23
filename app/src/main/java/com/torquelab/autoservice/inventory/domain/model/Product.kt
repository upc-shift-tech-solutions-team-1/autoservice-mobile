package com.torquelab.autoservice.inventory.domain.model

import java.math.BigDecimal

enum class ProductStatus {
    NORMAL,
    LOW_STOCK,
    OUT_OF_STOCK
}

data class Product(
    val id: Int = 0,
    val code: String = "",
    val name: String = "",
    val category: String = "SPARE_PART",
    val brand: String = "",
    val unitPrice: BigDecimal = BigDecimal.ZERO,
    val purchasePrice: BigDecimal = BigDecimal.ZERO,
    val currentStock: Int = 0,
    val minimumStock: Int = 3,
    val status: ProductStatus = ProductStatus.NORMAL,
    val image: String = "",
    val qualityTier: String = "STANDARD",
    val specification: String = "",
    val presentation: String = "",
    val unitMeasure: String = "UNIT"
) {
    val isLowStock: Boolean get() = currentStock <= minimumStock
    val effectiveStatus: ProductStatus get() = when {
        currentStock <= 0 -> ProductStatus.OUT_OF_STOCK
        currentStock <= minimumStock -> ProductStatus.LOW_STOCK
        else -> ProductStatus.NORMAL
    }

    fun decrementStock(quantity: Int): Product {
        if (quantity <= 0) throw IllegalArgumentException("La cantidad debe ser mayor a 0")
        if (currentStock - quantity < 0) {
            throw InsufficientStockException(availableStock = currentStock, requestedQuantity = quantity)
        }
        val newStock = currentStock - quantity
        val newStatus = if (newStock <= minimumStock) ProductStatus.LOW_STOCK else ProductStatus.NORMAL
        return copy(currentStock = newStock, status = newStatus)
    }

    fun incrementStock(quantity: Int): Product {
        if (quantity <= 0) throw IllegalArgumentException("La cantidad debe ser mayor a 0")
        val newStock = currentStock + quantity
        val newStatus = if (newStock <= minimumStock) ProductStatus.LOW_STOCK else ProductStatus.NORMAL
        return copy(currentStock = newStock, status = newStatus)
    }
}
