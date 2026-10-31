
package com.torquelab.autoservice.customer_trust

import com.torquelab.autoservice.customer_trust.data.remote.TrackingApi
import com.torquelab.autoservice.customer_trust.data.repository.TrackingRepositoryImpl
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderNotFoundException
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderStatus
import com.torquelab.autoservice.customer_trust.domain.model.TrackingTaskStatus
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TrackingApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: TrackingRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        repository = TrackingRepositoryImpl(
            retrofit.create(TrackingApi::class.java)
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun jsonResponse(
        body: String,
        code: Int = 200
    ): MockResponse =
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody(body)

    @Test
    fun `valid tracking code should return complete order with related data`() = runTest {
        // Arrange
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.requestUrl?.encodedPath) {
                    "/api/v1/tracking/workorders" -> jsonResponse(
                        """
                        [{
                          "id": 50,
                          "trackingCode": "TRK-100",
                          "workshopId": "T-01",
                          "vehicleId": 10,
                          "customerId": 20,
                          "description": "Mantenimiento preventivo",
                          "status": "IN_PROGRESS",
                          "price": 350.0,
                          "estimatedDate": "2026-10-30"
                        }]
                        """.trimIndent()
                    )

                    "/api/v1/tracking/vehicles/10" -> jsonResponse(
                        """
                        {
                          "brand": "Toyota",
                          "model": "Corolla",
                          "plate": "ABC-123"
                        }
                        """.trimIndent()
                    )

                    "/api/v1/tracking/tasks" -> jsonResponse(
                        """
                        [{
                          "id": 1,
                          "description": "Cambio de aceite",
                          "status": "COMPLETED",
                          "technicalDiagnosis": "Aceite desgastado",
                          "customerExplanation": "Se reemplazo el aceite",
                          "evidenceRegistered": null,
                          "laborPrice": 80.0,
                          "parts": []
                        }]
                        """.trimIndent()
                    )

                    "/api/v1/tracking/customers/20" -> jsonResponse(
                        """{"fullName":"Juan Perez"}"""
                    )

                    "/api/v1/tracking/workshops/T-01" -> jsonResponse(
                        """{"name":"AutoService Lima"}"""
                    )

                    else -> jsonResponse(
                        """{"message":"Ruta no encontrada"}""",
                        404
                    )
                }
            }
        }

        // Act
        val result = repository.getOrderByCode("TRK-100")

        // Assert
        assertTrue(result.isSuccess)

        val order = result.getOrThrow()

        assertEquals("TRK-100", order.trackingCode)
        assertEquals("Toyota", order.vehicleBrand)
        assertEquals("Corolla", order.vehicleModel)
        assertEquals("ABC-123", order.vehiclePlate)
        assertEquals("Juan Perez", order.customerName)
        assertEquals("AutoService Lima", order.workshopName)
        assertEquals("Mantenimiento preventivo", order.serviceDescription)
        assertEquals(TrackingOrderStatus.IN_PROGRESS, order.status)

        assertEquals(1, order.tasks.size)
        assertEquals("Cambio de aceite", order.tasks.first().description)
        assertEquals(TrackingTaskStatus.COMPLETED, order.tasks.first().status)

        assertEquals(100, order.progress)
        assertEquals(350.0, order.totalCost!!, 0.001)
        assertEquals("2026-10-30", order.estimatedDelivery)
        assertEquals(5, server.requestCount)
    }

    @Test
    fun `unknown tracking code should return business not found error`() = runTest {
        // Arrange
        server.enqueue(
            jsonResponse(
                """{"message":"Orden no encontrada"}""",
                404
            )
        )

        // Act
        val result = repository.getOrderByCode("TRK-404")

        // Assert
        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull() is TrackingOrderNotFoundException
        )

        val request = server.takeRequest()

        assertEquals("GET", request.method)
        assertEquals(
            "/api/v1/tracking/workorders?trackingCode=TRK-404",
            request.path
        )
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `tracking should preserve main order when vehicle service fails`() = runTest {
        // Arrange
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.requestUrl?.encodedPath) {
                    "/api/v1/tracking/workorders" -> jsonResponse(
                        """
                        [{
                          "id": 60,
                          "trackingCode": "TRK-200",
                          "workshopId": null,
                          "vehicleId": 99,
                          "customerId": 20,
                          "description": "Revision general",
                          "status": "PENDING",
                          "price": 150.0,
                          "estimatedDate": null
                        }]
                        """.trimIndent()
                    )

                    "/api/v1/tracking/vehicles/99" -> jsonResponse(
                        """{"message":"Servicio no disponible"}""",
                        500
                    )

                    "/api/v1/tracking/tasks" -> jsonResponse("[]")

                    "/api/v1/tracking/customers/20" -> jsonResponse(
                        """{"fullName":"Juan Perez"}"""
                    )

                    else -> jsonResponse(
                        """{"message":"Ruta no encontrada"}""",
                        404
                    )
                }
            }
        }

        // Act
        val result = repository.getOrderByCode("TRK-200")

        // Assert
        assertTrue(result.isSuccess)

        val order = result.getOrThrow()

        assertEquals("TRK-200", order.trackingCode)
        assertEquals(TrackingOrderStatus.PENDING, order.status)
        assertEquals("Juan Perez", order.customerName)
        assertNull(order.vehicleBrand)
        assertNull(order.vehicleModel)
        assertNull(order.vehiclePlate)
        assertNull(order.workshopName)
        assertTrue(order.tasks.isEmpty())
        assertEquals(0, order.progress)
        assertEquals(4, server.requestCount)
    }
}
