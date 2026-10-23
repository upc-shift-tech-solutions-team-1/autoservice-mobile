package com.torquelab.autoservice.inventory.domain.usecase

import com.torquelab.autoservice.inventory.domain.model.InsufficientStockException
import com.torquelab.autoservice.inventory.domain.model.MovementType
import com.torquelab.autoservice.inventory.domain.model.Product
import com.torquelab.autoservice.inventory.domain.model.ProductStatus
import com.torquelab.autoservice.inventory.domain.repository.InventoryRepository
import javax.inject.Inject

interface UpdateStockUseCase {
    suspend operator fun invoke(
        product: Product,
        quantity: Int,
        movementType: MovementType,
        reason: String = ""
    ): Product
}

class UpdateStockUseCaseImpl @Inject constructor(
    private val repository: InventoryRepository
) : UpdateStockUseCase {
    override suspend operator fun invoke(
        product: Product,
        quantity: Int,
        movementType: MovementType,
        reason: String
    ): Product {
        if (quantity <= 0) {
            throw IllegalArgumentException("La cantidad debe ser mayor a 0")
        }

        if (movementType == MovementType.OUT) {
            if (product.currentStock - quantity < 0) {
                throw InsufficientStockException(
                    availableStock = product.currentStock,
                    requestedQuantity = quantity
                )
            }
        }

        val newStock = when (movementType) {
            MovementType.IN -> product.currentStock + quantity
            MovementType.OUT -> product.currentStock - quantity
            MovementType.ADJUSTMENT -> quantity
        }

        val newStatus = if (newStock <= product.minimumStock) {
            ProductStatus.LOW_STOCK
        } else {
            ProductStatus.NORMAL
        }

        val updatedProduct = product.copy(
            currentStock = newStock,
            status = newStatus
        )

        val savedResult = repository.saveProduct(updatedProduct)
        return savedResult.getOrThrow()
    }
}
