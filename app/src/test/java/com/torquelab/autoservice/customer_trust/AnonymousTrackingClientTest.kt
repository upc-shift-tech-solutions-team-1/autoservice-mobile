package com.torquelab.autoservice.customer_trust

import com.torquelab.autoservice.customer_trust.di.TrackingModule
import com.torquelab.autoservice.shared.network.AuthInterceptor
import com.torquelab.autoservice.shared.session.SessionEventManager
import com.torquelab.autoservice.shared.session.SessionManager
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AnonymousTrackingClientTest {
    @Test fun `public client never sends stored token or expires authenticated session on anonymous 401`() = runTest {
        val server = MockWebServer().apply { start() }
        try {
            val session = mock(SessionManager::class.java)
            val events = mock(SessionEventManager::class.java)
            whenever(session.currentToken).thenReturn("test-only-token")
            val authenticated = OkHttpClient.Builder().addInterceptor(AuthInterceptor(session,events)).build()
            val retrofit = Retrofit.Builder().baseUrl(server.url("/api/v1/")).client(authenticated)
                .addConverterFactory(GsonConverterFactory.create()).build()
            val public = TrackingModule.provideTrackingApi(retrofit,authenticated)
            server.enqueue(MockResponse().setResponseCode(401))
            runCatching { public.getSummary("WO-TEST") }
            assertNull(server.takeRequest().getHeader("Authorization"))
            verify(events,never()).notifySessionExpired()
            assertEquals(1,authenticated.interceptors.size) // Do not mutate the shared authenticated client.
        } finally { server.shutdown() }
    }
}
