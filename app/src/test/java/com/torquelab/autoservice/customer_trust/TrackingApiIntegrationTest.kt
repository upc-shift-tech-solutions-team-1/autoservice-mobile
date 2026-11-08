package com.torquelab.autoservice.customer_trust

import com.torquelab.autoservice.customer_trust.data.remote.TrackingApi
import com.torquelab.autoservice.customer_trust.data.repository.TrackingRepositoryImpl
import com.torquelab.autoservice.customer_trust.domain.model.*
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TrackingApiIntegrationTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: TrackingRepositoryImpl
    @Before fun setup() {
        server = MockWebServer().apply { start() }
        repository = TrackingRepositoryImpl(Retrofit.Builder().baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create()).build().create(TrackingApi::class.java))
    }
    @After fun close() { server.shutdown() }
    private fun response(body: String, code: Int = 200) = MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body)
    private val summary = """{
        "trackingCode":"WO-TEST", "status":"IN_PROGRESS", "progressPercentage":50,
        "estimatedDate":"2026-11-15",
        "tasks":[{"description":"Cambio de aceite","status":"COMPLETED","technicalDiagnosis":"Aceite degradado",
          "customerExplanation":"Se reemplazó el aceite","laborPrice":80,"materialsCost":20,"totalCost":100,
          "parts":[{"name":"Aceite","quantity":1,"unitPrice":20}]}],
        "costs":{"laborSubtotal":80,"materialsSubtotal":20,"total":100},
        "history":[{"status":"PENDING","changedAtUtc":"2026-11-01T12:00:00Z"}],
        "estimatedDateHistory":[{"previousEstimatedDate":"2026-11-14","estimatedDate":"2026-11-15","reason":"Repuesto en traslado","changedAtUtc":"2026-11-02T12:00:00Z"}],
        "primaryMechanic":{"fullName":"Ana Pérez","specialty":"Motor"}
    }"""
    private fun configure(metadataFails: Boolean = false) {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when(request.requestUrl!!.encodedPath) {
                "/api/v1/tracking/summary" -> response(summary)
                "/api/v1/tracking/workorders" -> response("""[{"trackingCode":"WO-TEST","vehicleId":10,"customerId":20,"workshopId":"WS-01","price":999}]""")
                "/api/v1/tracking/vehicles/10" -> if (metadataFails) response("{}",500) else response("""{"brand":"Toyota","model":"Corolla","plate":"ABC-123"}""")
                "/api/v1/tracking/customers/20" -> response("""{"fullName":"Juan Pérez"}""")
                "/api/v1/tracking/workshops/WS-01" -> response("""{"name":"Taller Lima"}""")
                else -> response("{}",404)
            }
        }
    }
    @Test fun `summary owns progress and total and exposes dates mechanic and tasks without internal IDs`() = runTest {
        configure()
        val order = repository.getOrderByCode("WO-TEST").getOrThrow()
        assertEquals("ABC-123",order.vehiclePlate)
        assertEquals("Juan Pérez",order.customerName)
        assertEquals(50,order.progress) // One completed row must not replace server progress with 100.
        assertEquals(100.0,order.totalCost!!,0.001) // Metadata price 999 is never used.
        assertEquals("Repuesto en traslado",order.estimatedDateHistory.single().reason)
        assertEquals("Ana Pérez",order.primaryMechanic!!.fullName)
        assertEquals("Motor",order.primaryMechanic!!.specialty)
        assertEquals(TrackingTaskStatus.COMPLETED,order.tasks.single().status)
        assertEquals(1,order.tasks.single().id) // Local display key, not a database ID.
        assertEquals("Aceite",order.tasks.single().parts.single().name)
        assertEquals(5,server.requestCount)
        val requests = (1..server.requestCount).map { server.takeRequest() }
        assertTrue(requests.none { it.path!!.contains("tracking/tasks") })
        assertTrue(requests.all { it.getHeader("Authorization") == null })
    }
    @Test fun `unknown code returns not found from summary without metadata requests`() = runTest {
        server.enqueue(response("{}",404))
        val result = repository.getOrderByCode("WO-NOT-FOUND")
        assertTrue(result.exceptionOrNull() is TrackingOrderNotFoundException)
        assertEquals("/api/v1/tracking/summary?trackingCode=WO-NOT-FOUND",server.takeRequest().path)
        assertEquals(1,server.requestCount)
    }
    @Test fun `missing vehicle metadata does not hide summary data`() = runTest {
        configure(metadataFails = true)
        val order = repository.getOrderByCode("WO-TEST").getOrThrow()
        assertNull(order.vehicleBrand)
        assertEquals(100.0,order.totalCost!!,0.001)
        assertEquals(1,order.tasks.size)
        assertEquals("2026-11-15",order.estimatedDelivery)
    }
    @Test fun `unavailable summary is an error not a fake empty order`() = runTest {
        server.enqueue(response("{}",503))
        assertTrue(repository.getOrderByCode("WO-TEST").isFailure)
        assertEquals(1,server.requestCount)
    }
}
