package com.torquelab.autoservice.mechanic.integration

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.torquelab.autoservice.fleet.data.FleetApi
import com.torquelab.autoservice.inventory.data.InventoryApi
import com.torquelab.autoservice.mechanic.data.repository.MechanicRepositoryImpl
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTaskPartInput
import com.torquelab.autoservice.workshop.data.remote.TaskApi
import com.torquelab.autoservice.workshop.data.remote.WorkshopApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MechanicWorkshopApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var workshopApi: WorkshopApi
    private lateinit var taskApi: TaskApi
    private lateinit var fleetApi: FleetApi
    private lateinit var inventoryApi: InventoryApi
    private lateinit var repository: MechanicRepositoryImpl
    private val gson = Gson()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        workshopApi = retrofit.create(WorkshopApi::class.java)
        taskApi = retrofit.create(TaskApi::class.java)
        fleetApi = retrofit.create(FleetApi::class.java)
        inventoryApi = retrofit.create(InventoryApi::class.java)

        repository = MechanicRepositoryImpl(workshopApi, taskApi, fleetApi, inventoryApi)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `proposeTask sends HTTP POST with serialized body containing SUBMITTED status and parts`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody("""{"id": 99, "workOrderId": 12, "description": "Cambio de amortiguadores", "status": "PENDING"}""")
        )

        val input = MechanicProposeTaskInput(
            workOrderId = 12,
            mechanicId = 5,
            description = "Cambio de amortiguadores delanteros",
            priority = "URGENT",
            estimatedTime = 90,
            technicalDiagnosis = "Fuga severa de aceite hidráulico en amortiguador izquierdo",
            parts = listOf(
                MechanicTaskPartInput(inventoryItemId = 101, quantity = 2)
            )
        )

        val result = repository.proposeTask(input)
        assertTrue(result.isSuccess)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/tasks", recordedRequest.path)

        val json = gson.fromJson(recordedRequest.body.readUtf8(), JsonObject::class.java)
        assertEquals(12, json.get("workOrderId").asInt)
        assertEquals(5, json.get("mechanicId").asInt)
        assertEquals("Cambio de amortiguadores delanteros", json.get("description").asString)
        assertEquals("SUBMITTED", json.get("adminReviewStatus").asString)
        assertEquals("URGENT", json.get("priority").asString)

        val partsArray = json.getAsJsonArray("parts")
        assertEquals(1, partsArray.size())
        assertEquals(101, partsArray.get(0).asJsonObject.get("inventoryItemId").asInt)
        assertEquals(2, partsArray.get(0).asJsonObject.get("quantity").asInt)
    }

    @Test
    fun `startTask sends HTTP PATCH to task endpoint with status IN_PROGRESS`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val result = repository.startTask(44)
        assertTrue(result.isSuccess)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("PATCH", recordedRequest.method)
        assertEquals("/tasks/44", recordedRequest.path)

        val json = gson.fromJson(recordedRequest.body.readUtf8(), JsonObject::class.java)
        assertEquals("IN_PROGRESS", json.get("status").asString)
    }

    @Test
    fun `completeTask sends HTTP PATCH to task endpoint with status COMPLETED and technicalDiagnosis`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val diagnosis = "Reparación concluida exitosamente con prueba en ruta"
        val result = repository.completeTask(44, diagnosis)
        assertTrue(result.isSuccess)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("PATCH", recordedRequest.method)
        assertEquals("/tasks/44", recordedRequest.path)

        val json = gson.fromJson(recordedRequest.body.readUtf8(), JsonObject::class.java)
        assertEquals("COMPLETED", json.get("status").asString)
        assertEquals(diagnosis, json.get("technicalDiagnosis").asString)
    }

    @Test
    fun `getOrdersForMechanic integrates workorders, vehicles and tasks across HTTP responses`() = runTest {
        // Response 1: WorkshopApi.getWorkOrders()
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                [
                    {
                        "id": 1,
                        "workshopId": "WS-01",
                        "trackingCode": "TRK-001",
                        "vehicleId": 10,
                        "customerId": 2,
                        "mechanicId": 5,
                        "description": "Frenos y afinamiento",
                        "status": "IN_PROGRESS",
                        "price": 320.0,
                        "estimatedDate": "2026-10-25",
                        "startDate": "2026-10-24",
                        "tasksCompleted": false,
                        "sparePartsChecked": true,
                        "diagnosisValidated": true,
                        "cleaningDone": false,
                        "finalTestDone": false
                    }
                ]
                """.trimIndent()
            )
        )

        // Response 2: FleetApi.vehicles()
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                [
                    {
                        "id": 10,
                        "plate": "ABC-123",
                        "brand": "Toyota",
                        "model": "Corolla",
                        "year": "2022",
                        "color": "Blanco",
                        "status": "IN_WORKSHOP",
                        "customerId": 2
                    }
                ]
                """.trimIndent()
            )
        )

        // Response 3: TaskApi.getTasks()
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                [
                    {
                        "id": 101,
                        "workOrderId": 1,
                        "mechanicId": 5,
                        "description": "Cambio de pastillas",
                        "status": "COMPLETED",
                        "priority": "HIGH",
                        "estimatedTime": 60,
                        "laborPrice": 80.0,
                        "materialsCost": 120.0
                    },
                    {
                        "id": 102,
                        "workOrderId": 1,
                        "mechanicId": 5,
                        "description": "Purgado de frenos",
                        "status": "IN_PROGRESS",
                        "priority": "MEDIUM",
                        "estimatedTime": 30,
                        "laborPrice": 40.0,
                        "materialsCost": 20.0
                    }
                ]
                """.trimIndent()
            )
        )

        val result = repository.getOrdersForMechanic(5)

        assertTrue(result.isSuccess)
        val orders = result.getOrNull()!!
        assertEquals(1, orders.size)

        val order = orders[0]
        assertEquals("Toyota Corolla", order.vehicleName)
        assertEquals("ABC-123", order.vehiclePlate)
        assertEquals(1, order.tasksCompleted)
        assertEquals(2, order.totalTasks)
        assertEquals(50, order.progress)
        assertEquals(120.0, order.totalLaborCost, 0.001)
        assertEquals(140.0, order.totalMaterialsCost, 0.001)
    }
}
