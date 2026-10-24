package com.torquelab.autoservice.workshop.integration

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.torquelab.autoservice.customer_management.data.remote.CustomerApi
import com.torquelab.autoservice.fleet.data.FleetApi
import com.torquelab.autoservice.staff.data.remote.StaffApi
import com.torquelab.autoservice.workshop.data.remote.TaskApi
import com.torquelab.autoservice.workshop.data.remote.WorkshopApi
import com.torquelab.autoservice.workshop.data.repository.WorkshopRepositoryImpl
import com.torquelab.autoservice.workshop.domain.model.CreateTaskInput
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.UpdateTaskInput
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

class WorkshopOperationsApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var workshopApi: WorkshopApi
    private lateinit var taskApi: TaskApi
    private lateinit var customerApi: CustomerApi
    private lateinit var fleetApi: FleetApi
    private lateinit var staffApi: StaffApi
    private lateinit var repository: WorkshopRepositoryImpl
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
        customerApi = retrofit.create(CustomerApi::class.java)
        fleetApi = retrofit.create(FleetApi::class.java)
        staffApi = retrofit.create(StaffApi::class.java)

        repository = WorkshopRepositoryImpl(workshopApi, taskApi, customerApi, fleetApi, staffApi)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `createTask sends HTTP POST with CreateTaskRequest body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody("""{"id": 88}"""))

        val input = CreateTaskInput(
            orderId = 15,
            description = "Diagnóstico con scanner OBD2",
            priority = "HIGH",
            estimatedMinutes = 30,
            laborPrice = 75.0,
            mechanicId = 3
        )

        val result = repository.createTask(input)
        assertTrue(result.isSuccess)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/tasks", request.path)

        val body = gson.fromJson(request.body.readUtf8(), JsonObject::class.java)
        assertEquals(15, body.get("workOrderId").asInt)
        assertEquals("Diagnóstico con scanner OBD2", body.get("description").asString)
        assertEquals("HIGH", body.get("priority").asString)
        assertEquals(30, body.get("estimatedTime").asInt)
        assertEquals(75.0, body.get("laborPrice").asDouble, 0.001)
        assertEquals(3, body.get("mechanicId").asInt)
    }

    @Test
    fun `updateTask sends HTTP PUT to task endpoint with UpdateTaskRequest body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"id": 88}"""))

        val input = UpdateTaskInput(
            taskId = 88,
            description = "Diagnóstico finalizado",
            priority = "MEDIUM",
            estimatedMinutes = 35,
            laborPrice = 80.0,
            mechanicId = 3,
            status = TaskStatus.COMPLETED
        )

        val result = repository.updateTask(input)
        assertTrue(result.isSuccess)

        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/tasks/88", request.path)

        val body = gson.fromJson(request.body.readUtf8(), JsonObject::class.java)
        assertEquals("Diagnóstico finalizado", body.get("description").asString)
        assertEquals("COMPLETED", body.get("status").asString)
    }

    @Test
    fun `cancelTask sends HTTP DELETE to task endpoint`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val result = repository.cancelTask(88)
        assertTrue(result.isSuccess)

        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/tasks/88", request.path)
    }

    @Test
    fun `getFinancialSummary sends HTTP GET and parses complete financial indicators`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                    "projectedRevenue": 30000.0,
                    "realizedRevenue": 22000.0,
                    "pendingRevenue": 8000.0,
                    "operatingCost": 12000.0,
                    "grossProfit": 10000.0,
                    "marginPercentage": 45.45,
                    "profitableOrders": 25,
                    "lossOrders": 2,
                    "averageTicket": 1200.0
                }
                """.trimIndent()
            )
        )

        val result = repository.getFinancialSummary()

        assertTrue(result.isSuccess)
        val summary = result.getOrNull()!!
        assertEquals(30000.0, summary.projectedRevenue, 0.001)
        assertEquals(22000.0, summary.realizedRevenue, 0.001)
        assertEquals(8000.0, summary.pendingRevenue, 0.001)
        assertEquals(10000.0, summary.grossProfit, 0.001)
        assertEquals(45.45, summary.marginPercentage, 0.001)
        assertEquals(25, summary.profitableOrders)
        assertEquals(2, summary.lossOrders)
    }
}
