package com.torquelab.autoservice.inventory

import com.torquelab.autoservice.inventory.data.remote.InventoryRemoteDataSource
import com.torquelab.autoservice.inventory.data.remote.dto.ProductDto
import com.torquelab.autoservice.inventory.data.repository.InventoryRepositoryImpl
import com.torquelab.autoservice.inventory.domain.model.ProductStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryRepositoryImplTest {

    private lateinit var remoteDataSource: InventoryRemoteDataSource
    private lateinit var repository: InventoryRepositoryImpl

    @Before
    fun setUp() {
        remoteDataSource = mock(InventoryRemoteDataSource::class.java)
        repository = InventoryRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getProducts fetches dtos from datasource and maps correctly to domain Product entities`() = runTest {
        val dtoList = listOf(
            ProductDto(
                id = 1,
                code = "PRD-001",
                name = "Bujía Iri",
                category = "SPARE_PART",
                brand = "NGK",
                unitPrice = BigDecimal("35.00"),
                purchasePrice = BigDecimal("20.00"),
                currentStock = 2,
                minimumStock = 5,
                status = "LOW_STOCK"
            ),
            ProductDto(
                id = 2,
                code = "PRD-002",
                name = "Pastillas de Freno",
                category = "SPARE_PART",
                brand = "Bosch",
                unitPrice = BigDecimal("150.00"),
                purchasePrice = BigDecimal("100.00"),
                currentStock = 20,
                minimumStock = 5,
                status = "NORMAL"
            )
        )

        whenever(remoteDataSource.getProducts()).thenReturn(dtoList)

        val result = repository.getProducts()

        assertTrue(result.isSuccess)
        val products = result.getOrNull()!!
        assertEquals(2, products.size)

        // Verify product 1 mapping
        val p1 = products[0]
        assertEquals(1, p1.id)
        assertEquals("PRD-001", p1.code)
        assertEquals("Bujía Iri", p1.name)
        assertEquals(2, p1.currentStock)
        assertEquals(5, p1.minimumStock)
        assertEquals(ProductStatus.LOW_STOCK, p1.status)
        assertTrue(p1.isLowStock)

        // Verify product 2 mapping
        val p2 = products[1]
        assertEquals(2, p2.id)
        assertEquals("PRD-002", p2.code)
        assertEquals("Pastillas de Freno", p2.name)
        assertEquals(20, p2.currentStock)
        assertEquals(5, p2.minimumStock)
        assertEquals(ProductStatus.NORMAL, p2.status)

        verify(remoteDataSource).getProducts()
    }
}
