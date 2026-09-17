package com.torquelab.autoservice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.torquelab.autoservice.shared.navigation.AppNavHost
import com.torquelab.autoservice.shared.session.SessionEventManager
import com.torquelab.autoservice.shared.session.SessionManager
import com.torquelab.autoservice.ui.theme.AutoServiceTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var sessionEventManager: SessionEventManager

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            AutoServiceTheme {

                val navController =
                    rememberNavController()

                AppNavHost(
                    navController =
                        navController,
                    sessionManager =
                        sessionManager,
                    sessionEventManager =
                        sessionEventManager
                )
            }
        }
    }
}