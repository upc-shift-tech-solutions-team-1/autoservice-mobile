package com.torquelab.autoservice.shared.network

import com.torquelab.autoservice.shared.session.SessionEventManager
import com.torquelab.autoservice.shared.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val sessionEventManager: SessionEventManager
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {

        val originalRequest =
            chain.request()

        val token =
            sessionManager.currentToken

        val request =
            if (token.isNullOrBlank()) {

                originalRequest

            } else {

                originalRequest
                    .newBuilder()
                    .header(
                        "Authorization",
                        "Bearer $token"
                    )
                    .build()
            }

        val response =
            chain.proceed(request)

        val requestWasAuthenticated =
            request.header(
                "Authorization"
            ) != null

        if (
            response.code == 401 &&
            requestWasAuthenticated
        ) {

            sessionEventManager
                .notifySessionExpired()
        }

        return response
    }
}