package com.torquelab.autoservice.shared.network

import kotlinx.coroutines.CancellationException

/** Cancellation belongs to the coroutine lifecycle, never to an error displayed to the user. */
suspend fun <T> apiResult(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Exception) {
    Result.failure(error)
}
