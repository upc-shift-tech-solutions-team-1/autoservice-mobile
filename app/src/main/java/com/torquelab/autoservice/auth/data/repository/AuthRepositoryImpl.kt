package com.torquelab.autoservice.auth.data.repository

import com.torquelab.autoservice.auth.data.remote.AuthApi
import com.torquelab.autoservice.auth.data.remote.dto.SignInRequest
import com.torquelab.autoservice.auth.domain.repository.AuthRepository
import com.torquelab.autoservice.shared.session.SessionManager
import com.torquelab.autoservice.shared.session.UserSession
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import com.torquelab.autoservice.auth.data.remote.dto.RegisterWorkshopRequest
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<UserSession> {

        return try {

            val response = authApi.signIn(
                SignInRequest(
                    email = email,
                    password = password
                )
            )

            val session = UserSession(
                userId = response.id,
                email = response.email,
                role = response.role,
                workshopId = response.workshopId,
                mechanicId = response.mechanicId,
                token = response.token
            )

            sessionManager.saveSession(session)

            Result.success(session)

        } catch (exception: HttpException) {

            Result.failure(
                Exception(
                    when (exception.code()) {
                        401 -> "Incorrect email or password."
                        else -> "Authentication failed."
                    }
                )
            )

        } catch (exception: IOException) {

            Result.failure(
                Exception("Unable to connect to AutoService.")
            )

        } catch (exception: Exception) {

            Result.failure(
                Exception(
                    exception.message ?: "Unexpected authentication error."
                )
            )
        }
    }

    override suspend fun registerWorkshop(
        workshopName: String,
        email: String,
        password: String
    ): Result<Unit> {

        return try {

            authApi.registerWorkshop(
                RegisterWorkshopRequest(
                    workshopName = workshopName,
                    email = email,
                    password = password
                )
            )

            Result.success(Unit)

        } catch (exception: HttpException) {

            val message = when (exception.code()) {
                400 -> "Unable to register the workshop. The email may already be registered."
                else -> "Workshop registration failed."
            }

            Result.failure(Exception(message))

        } catch (exception: IOException) {

            Result.failure(
                Exception("Unable to connect to AutoService.")
            )

        } catch (exception: Exception) {

            Result.failure(
                Exception(
                    exception.message ?: "Unexpected registration error."
                )
            )
        }
    }



}