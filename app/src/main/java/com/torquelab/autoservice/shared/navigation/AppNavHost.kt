package com.torquelab.autoservice.shared.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.torquelab.autoservice.shared.session.SessionManager

@Composable
fun AppNavHost(
    navController: NavHostController,
    sessionManager: SessionManager
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route
    ) {

        composable(AppRoute.Splash.route) {

            SplashScreen(
                sessionManager = sessionManager,

                onAuthenticated = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Splash.route) {
                            inclusive = true
                        }
                    }
                },

                onUnauthenticated = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(AppRoute.Login.route) {
            LoginPlaceholderScreen()
        }

        composable(AppRoute.Home.route) {
            HomePlaceholderScreen()
        }
    }
}