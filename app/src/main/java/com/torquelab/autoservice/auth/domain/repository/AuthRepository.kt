package com.torquelab.autoservice.auth.domain.repository

import com.torquelab.autoservice.shared.session.UserSession

interface AuthRepository {

    suspend fun signIn(
        email: String,
        password: String
    ): Result<UserSession>

    suspend fun registerWorkshop(
        workshopName: String,
        email: String,
        password: String
    ): Result<Unit>


}