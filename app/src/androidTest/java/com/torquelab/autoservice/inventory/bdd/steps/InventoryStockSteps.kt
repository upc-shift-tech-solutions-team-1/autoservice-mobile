
package com.torquelab.autoservice.inventory.bdd.steps

import com.torquelab.autoservice.inventory.domain.model.InsufficientStockException
import com.torquelab.autoservice.inventory.domain.model.MovementType
import com.torquelab.autoservice.inventory.domain.model.Product
import com.torquelab.autoservice.inventory.domain.model.ProductStatus
import com.torquelab.autoservice.inventory.domain.repository.InventoryRepository
import com.torquelab.autoservice.inventory.domain.usecase.UpdateStockUseCaseImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.math.BigDecimal

class InventoryStockSteps {

    private var product: Product? = null
    private var requestedQuantity: Int = 0
    private var updatedProduct: Product? = null
    private var operationError: Throwable? = null
    private var saveCount: Int = 0

    private val repository = object : InventoryRepository {

        override suspend fun getProducts(): Result<List<Product>> =
            Result.success(listOfNotNull(product))

        override suspend fun getProductById(id: Int): Result<Product?> =
            Result.success(product?.takeIf { it.id == id })

        override suspend fun saveProduct(product: Product): Result<Product> {
            saveCount++
            this@InventoryStockSteps.product = product
            return Result.success(product)
        }
    }

    private val useCase = UpdateStockUseCaseImpl(repository)

    fun givenProductWithStock(stock: Int) {
        product = Product(
            id = 1,
            code = "P-001",
            name = "Filtro de aceite",
            currentStock = stock,
            minimumStock = 2,
            status = ProductStatus.NORMAL,
            unitPrice = BigDecimal("25.00")
        )

        saveCount = 0
        updatedProduct = null
        operationError = null
    }

    fun givenRequestedQuantity(quantity: Int) {
        requestedQuantity = quantity
    }

    suspend fun whenReceivingMaterials() {
        val currentProduct = requireNotNull(product)

        val result = runCatching {
            useCase.invoke(
                product = currentProduct,
                quantity = requestedQuantity,
                movementType = MovementType.IN,
                reason = "Recepción de proveedor"
            )
        }

        updatedProduct = result.getOrNull()
        operationError = result.exceptionOrNull()
    }

    suspend fun whenConsumingMaterials() {
        val currentProduct = requireNotNull(product)

        val result = runCatching {
            useCase.invoke(
                product = currentProduct,
                quantity = requestedQuantity,
                movementType = MovementType.OUT,
                reason = "Consumo para mantenimiento"
            )
        }

        updatedProduct = result.getOrNull()
        operationError = result.exceptionOrNull()
    }

    fun thenStockShouldBe(expectedStock: Int) {
        assertTrue(
            "La recepción debe realizarse correctamente",
            operationError == null
        )
        assertEquals(expectedStock, updatedProduct?.currentStock)
        assertEquals(expectedStock, product?.currentStock)
    }

    fun thenProductShouldBeSaved() {
        assertEquals(1, saveCount)
    }

    fun thenOperationShouldBeRejectedForInsufficientStock() {
        assertTrue(operationError is InsufficientStockException)
        assertEquals(null, updatedProduct)
    }

    fun thenNoChangesShouldBeSaved() {
        assertEquals(0, saveCount)
    }
}
