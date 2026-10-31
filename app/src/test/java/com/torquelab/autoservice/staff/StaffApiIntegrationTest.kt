
package com.torquelab.autoservice.staff

import com.google.gson.Gson
import com.torquelab.autoservice.staff.data.remote.StaffApi
import com.torquelab.autoservice.staff.data.repository.StaffRepositoryImpl
import com.torquelab.autoservice.staff.domain.model.CreateMechanicInput
import com.torquelab.autoservice.workshop.data.remote.TaskApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StaffApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: StaffRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        repository = StaffRepositoryImpl(
            staffApi = retrofit.create(StaffApi::class.java),
            taskApi = retrofit.create(TaskApi::class.java)
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

    private fun mechanicJson(capacity: Int = 3) = """
        {
            "id": 10,
            "fullName": "Ana Rojas",
            "email": "ana@example.com",
            "specialty": "Diagnostico",
            "maxCapacity": $capacity,
            "workshopId": "WS-1"
        }
    """.trimIndent()

    private fun taskJson(id: Int, status: String) = """
        {
            "id": $id,
            "workOrderId": 100,
            "mechanicId": 10,
            "description": "Revision de vehiculo",
            "status": "$status",
            "priority": "MEDIUM",
            "estimatedTime": 60,
            "laborPrice": 50.0
        }
    """.trimIndent()

    @Test
    fun `get mechanic should calculate availability from API tasks`() = runTest {
        // Arrange
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(
                request: RecordedRequest
            ): MockResponse = when (request.requestUrl?.encodedPath) {
                "/api/v1/mechanics/10" ->
                    jsonResponse(mechanicJson(capacity = 3))

                "/api/v1/tasks" -> {
                    val tasks = """
                        [
                            ${taskJson(1, "PENDING")},
                            ${taskJson(2, "IN_PROGRESS")},
                            ${taskJson(3, "COMPLETED")}
                        ]
                    """.trimIndent()
                    jsonResponse(tasks)
                }

                else -> jsonResponse(
                    """{"message":"Not found"}""",
                    404
                )
            }
        }

        // Act
        val result = repository.getMechanic(10)

        // Assert
        assertTrue(result.isSuccess)

        val mechanic = result.getOrThrow()

        assertEquals("Ana Rojas", mechanic.fullName)
        assertEquals(3, mechanic.maxCapacity)
        assertEquals(2, mechanic.assignedTasks)
        assertTrue(mechanic.isAvailable)

        assertEquals(2, server.requestCount)
    }

    @Test
    fun `mechanic at capacity should be unavailable after API consultation`() = runTest {
        // Arrange
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(
                request: RecordedRequest
            ): MockResponse = when (request.requestUrl?.encodedPath) {
                "/api/v1/mechanics/10" ->
                    jsonResponse(mechanicJson(capacity = 2))

                "/api/v1/tasks" -> jsonResponse(
                    """
                    [
                        ${taskJson(1, "PENDING")},
                        ${taskJson(2, "IN_PROGRESS")}
                    ]
                    """.trimIndent()
                )

                else -> jsonResponse(
                    """{"message":"Not found"}""",
                    404
                )
            }
        }

        // Act
        val result = repository.getMechanic(10)

        // Assert
        assertTrue(result.isSuccess)

        val mechanic = result.getOrThrow()

        assertEquals(2, mechanic.assignedTasks)
        assertEquals(2, mechanic.maxCapacity)
        assertFalse(mechanic.isAvailable)
    }

    @Test
    fun `register mechanic should send correct data to API`() = runTest {
        // Arrange
        server.enqueue(
            jsonResponse(
                mechanicJson(),
                201
            )
        )

        val input = CreateMechanicInput(
            fullName = "Ana Rojas",
            email = "ana@example.com",
            specialty = "Diagnostico",
            maxCapacity = 3,
            password = "Password123"
        )

        // Act
        val result = repository.registerMechanic(input)

        // Assert
        assertTrue(result.isSuccess)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/mechanics", request.path)

        val body = Gson()
            .fromJson(request.body.readUtf8(), Map::class.java)

        assertEquals("Ana Rojas", body["fullName"])
        assertEquals("ana@example.com", body["email"])
        assertEquals("Diagnostico", body["specialty"])
        assertEquals("Password123", body["password"])
        assertEquals(3.0, body["maxCapacity"])

        assertEquals(1, server.requestCount)
    }
}
