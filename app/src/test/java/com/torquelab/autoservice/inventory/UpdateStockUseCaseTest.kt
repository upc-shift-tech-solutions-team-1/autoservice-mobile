package com.torquelab.autoservice.inventory

import com.torquelab.autoservice.inventory.domain.model.InsufficientStockException
import com.torquelab.autoservice.inventory.domain.model.MovementType
import com.torquelab.autoservice.inventory.domain.model.Product
import com.torquelab.autoservice.inventory.domain.model.ProductStatus
import com.torquelab.autoservice.inventory.domain.repository.InventoryRepository
import com.torquelab.autoservice.inventory.domain.usecase.UpdateStockUseCaseImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateStockUseCaseTest {

    private lateinit var repository: InventoryRepository
    private lateinit var useCase: UpdateStockUseCaseImpl

    @Before
    fun setUp() {
        repository = mock(InventoryRepository::class.java)
        useCase = UpdateStockUseCaseImpl(repository)
    }

    @Test(expected = InsufficientStockException::class)
    fun `when OUT movement quantity exceeds current stock throws InsufficientStockException and does not save to repository`() = runTest {
        val product = Product(
            id = 10,
            code = "P-001",
            name = "Filtro de Aceite",
            currentStock = 5,
            minimumStock = 2,
            unitPrice = BigDecimal("25.00")
        )
        val requestedQuantity = 10 // Exceeds available stock (5)

        try {
            useCase.invoke(
                product = product,
                quantity = requestedQuantity,
                movementType = MovementType.OUT,
                reason = "Salida por orden de servicio"
            )
        } finally {
            verify(repository, never()).saveProduct(any())
        }
    }

    @Test
    fun `when stock reaches or falls below minimum limit marks product status as LOW_STOCK`() = runTest {
        val product = Product(
            id = 12,
            code = "P-002",
            name = "Aceite 5W-30",
            currentStock = 5,
            minimumStock = 3,
            status = ProductStatus.NORMAL,
            unitPrice = BigDecimal("80.00")
        )
        val requestedQuantity = 3 // New stock will be 5 - 3 = 2, which is <= minimumStock (3)

        val expectedUpdatedProduct = product.copy(
            currentStock = 2,
            status = ProductStatus.LOW_STOCK
        )

        whenever(repository.saveProduct(expectedUpdatedProduct))
            .thenReturn(Result.success(expectedUpdatedProduct))

        val result = useCase.invoke(
            product = product,
            quantity = requestedQuantity,
            movementType = MovementType.OUT,
            reason = "Consumo en mantenimiento"
        )

        assertEquals(2, result.currentStock)
        assertEquals(ProductStatus.LOW_STOCK, result.status)
        assertTrue(result.isLowStock)
        verify(repository).saveProduct(expectedUpdatedProduct)
    }
}
