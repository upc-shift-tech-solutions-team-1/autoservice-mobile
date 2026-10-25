
package com.torquelab.autoservice.inventory

import com.google.gson.Gson
import com.torquelab.autoservice.inventory.data.RemoteInventoryRepository
import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.inventory.domain.StockReceipt
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal

class InventoryApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: RemoteInventoryRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        repository = RemoteInventoryRepository(retrofit)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun createItem(stock: Int = 5) = InventoryItem(
        id = 10,
        code = "P-010",
        name = "Filtro de aceite",
        category = "SPARE_PART",
        brand = "Bosch",
        unitPrice = BigDecimal("35.00"),
        purchasePrice = BigDecimal("20.00"),
        minStock = 2,
        stock = stock
    )

    @Test
    fun `get inventory items should return catalog from API`() = runTest {
        // Arrange
        val item = createItem()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(listOf(item)))
        )

        // Act
        val result = repository.items()

        // Assert
        assertEquals(1, result.size)
        assertEquals(10, result.first().id)
        assertEquals("Filtro de aceite", result.first().name)
        assertEquals(5, result.first().stock)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/v1/inventoryitems", request.path)
    }

    @Test
    fun `receive stock should send provider receipt and return updated item`() = runTest {
        // Arrange
        val updatedItem = createItem(stock = 10)

        val responseBody = """
            {
                "item": ${Gson().toJson(updatedItem)}
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody)
        )

        val receipt = StockReceipt(
            quantity = 5,
            providerName = "Bosch",
            documentNumber = "FAC-001",
            notes = "Recepcion de materiales"
        )

        // Act
        val result = repository.receive(10, receipt)

        // Assert
        assertEquals(10, result.stock)
        assertEquals("Filtro de aceite", result.name)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/inventoryitems/10/receipts", request.path)

        val body = request.body.readUtf8()

        assertTrue(body.contains("\"quantity\":5"))
        assertTrue(body.contains("\"providerName\":\"Bosch\""))
        assertTrue(body.contains("\"documentNumber\":\"FAC-001\""))
    }

    @Test
    fun `receive stock should propagate bad request from API`() = runTest {
        // Arrange
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """{"message":"La cantidad recibida debe ser mayor que cero."}"""
                )
        )

        val invalidReceipt = StockReceipt(
            quantity = 0,
            providerName = "Bosch"
        )

        // Act
        val result = runCatching {
            repository.receive(10, invalidReceipt)
        }

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)

        val exception = result.exceptionOrNull() as HttpException
        assertEquals(400, exception.code())

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/inventoryitems/10/receipts", request.path)
    }
}
