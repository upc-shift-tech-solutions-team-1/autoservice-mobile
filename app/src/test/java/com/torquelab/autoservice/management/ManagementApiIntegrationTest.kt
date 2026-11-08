package com.torquelab.autoservice.management

import com.torquelab.autoservice.fleet.data.RemoteFleetRepository
import com.torquelab.autoservice.inventory.data.RemoteInventoryRepository
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ManagementApiIntegrationTest {
    @Test fun `catalog deletion uses existing tenant API routes and no request body`() = runTest {
        val server = MockWebServer().apply { start() }
        try {
            val retrofit = Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .addConverterFactory(GsonConverterFactory.create()).build()
            server.enqueue(MockResponse().setResponseCode(204))
            RemoteFleetRepository(retrofit).delete(7)
            val vehicle = server.takeRequest()
            assertEquals("DELETE",vehicle.method)
            assertEquals("/api/v1/vehicles/7",vehicle.path)
            assertEquals(0L,vehicle.bodySize)
            server.enqueue(MockResponse().setResponseCode(204))
            RemoteInventoryRepository(retrofit).delete(9)
            val item = server.takeRequest()
            assertEquals("DELETE",item.method)
            assertEquals("/api/v1/inventoryitems/9",item.path)
            assertEquals(0L,item.bodySize)
        } finally { server.shutdown() }
    }
}
