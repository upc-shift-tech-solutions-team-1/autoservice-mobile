package com.torquelab.autoservice.inventory.data.remote.dto

import com.torquelab.autoservice.inventory.domain.model.Product
import com.torquelab.autoservice.inventory.domain.model.ProductStatus
import java.math.BigDecimal

data class ProductDto(
    val id: Int = 0,
    val code: String = "",
    val name: String = "",
    val category: String = "SPARE_PART",
    val brand: String = "",
    val unitPrice: BigDecimal = BigDecimal.ZERO,
    val purchasePrice: BigDecimal = BigDecimal.ZERO,
    val currentStock: Int = 0,
    val minimumStock: Int = 3,
    val status: String = "NORMAL",
    val image: String = "",
    val qualityTier: String = "STANDARD",
    val specification: String = "",
    val presentation: String = "",
    val unitMeasure: String = "UNIT"
) {
    fun toDomain(): Product {
        val derivedStatus = when {
            currentStock <= 0 -> ProductStatus.OUT_OF_STOCK
            currentStock <= minimumStock -> ProductStatus.LOW_STOCK
            else -> runCatching { ProductStatus.valueOf(status) }.getOrDefault(ProductStatus.NORMAL)
        }
        return Product(
            id = id,
            code = code,
            name = name,
            category = category,
            brand = brand,
            unitPrice = unitPrice,
            purchasePrice = purchasePrice,
            currentStock = currentStock,
            minimumStock = minimumStock,
            status = derivedStatus,
            image = image,
            qualityTier = qualityTier,
            specification = specification,
            presentation = presentation,
            unitMeasure = unitMeasure
        )
    }

    companion object {
        fun fromDomain(product: Product): ProductDto = ProductDto(
            id = product.id,
            code = product.code,
            name = product.name,
            category = product.category,
            brand = product.brand,
            unitPrice = product.unitPrice,
            purchasePrice = product.purchasePrice,
            currentStock = product.currentStock,
            minimumStock = product.minimumStock,
            status = product.status.name,
            image = product.image,
            qualityTier = product.qualityTier,
            specification = product.specification,
            presentation = product.presentation,
            unitMeasure = product.unitMeasure
        )
    }
}
