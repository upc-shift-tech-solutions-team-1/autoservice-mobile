package com.torquelab.autoservice.auth.data.remote

import com.torquelab.autoservice.auth.data.remote.dto.RegisterWorkshopRequest
import com.torquelab.autoservice.auth.data.remote.dto.RegisterWorkshopResponse
import com.torquelab.autoservice.auth.data.remote.dto.SignInRequest
import com.torquelab.autoservice.auth.data.remote.dto.SignInResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/sign-in")
    suspend fun signIn(
        @Body request: SignInRequest
    ): SignInResponse

    @POST("auth/register-workshop")
    suspend fun registerWorkshop(
        @Body request: RegisterWorkshopRequest
    ): RegisterWorkshopResponse
}