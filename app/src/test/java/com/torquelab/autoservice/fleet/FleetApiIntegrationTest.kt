
package com.torquelab.autoservice.fleet

import com.google.gson.Gson
import com.torquelab.autoservice.fleet.data.RemoteFleetRepository
import com.torquelab.autoservice.fleet.domain.Vehicle
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

class FleetApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: RemoteFleetRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        repository = RemoteFleetRepository(retrofit)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun createVehicle() = Vehicle(
        plate = "ABC-123",
        brand = "Toyota",
        model = "Corolla",
        year = "2022",
        color = "White",
        customerId = 1,
        currentKilometers = 15000
    )

    @Test
    fun `get vehicles should return items from API`() = runTest {
        // Arrange
        val vehicle = createVehicle().copy(id = 7)

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(listOf(vehicle)))
        )

        // Act
        val result = repository.vehicles()

        // Assert
        assertEquals(1, result.size)
        assertEquals(7, result.first().id)
        assertEquals("ABC-123", result.first().plate)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/v1/vehicles", request.path)
    }

    @Test
    fun `create vehicle should send normalized data without id`() = runTest {
        // Arrange
        val vehicle = createVehicle().copy(
            plate = " abc-123 ",
            brand = " Toyota ",
            model = " Corolla "
        )

        val savedVehicle = createVehicle().copy(id = 12)

        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(savedVehicle))
        )

        // Act
        val result = repository.save(vehicle)

        // Assert
        assertEquals(12, result.id)
        assertEquals("ABC-123", result.plate)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/vehicles", request.path)

        val body = request.body.readUtf8()

        assertTrue(body.contains("\"plate\":\"ABC-123\""))
        assertTrue(body.contains("\"brand\":\"Toyota\""))
        assertTrue(body.contains("\"model\":\"Corolla\""))
        assertFalse(body.contains("\"id\":"))
    }

    @Test
    fun `create vehicle should propagate server error`() = runTest {
        // Arrange
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"message":"Internal server error"}""")
        )

        // Act & Assert
        val exception = assertThrows(HttpException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.save(createVehicle())
            }
        }

        assertEquals(500, exception.code())

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/vehicles", request.path)
    }
}
