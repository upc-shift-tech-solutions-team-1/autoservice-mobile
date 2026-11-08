package com.torquelab.autoservice.shared

import com.torquelab.autoservice.shared.network.apiResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ApiResultTest {
    @Test fun `coroutine cancellation propagates instead of becoming a recoverable network failure`() = runTest {
        try {
            apiResult<Unit> { throw CancellationException("cancelled test") }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }
    @Test fun `ordinary server error is preserved in Result`() = runTest {
        val error = IllegalStateException("Stock insuficiente")
        assertSame(error,apiResult<Unit> { throw error }.exceptionOrNull())
    }
}
