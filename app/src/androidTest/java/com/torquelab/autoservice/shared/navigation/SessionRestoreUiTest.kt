package com.torquelab.autoservice.shared.navigation

import android.os.Looper
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.torquelab.autoservice.shared.session.SessionManager
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Reads local session state only; does not sign in, clear the session or call the API. */
@RunWith(AndroidJUnit4::class)
class SessionRestoreUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun restoredSessionDispatchesNavigationOnAndroidMainThread() {
        val completed = AtomicBoolean(false)
        val sessionManager = SessionManager(InstrumentationRegistry.getInstrumentation().targetContext)
        val verifyNavigationThread = {
            assertSame(Looper.getMainLooper(), Looper.myLooper())
            completed.set(true)
        }
        compose.setContent {
            SplashScreen(sessionManager, { verifyNavigationThread() }, { verifyNavigationThread() })
        }
        compose.waitUntil(10_000) { completed.get() }
    }
}
